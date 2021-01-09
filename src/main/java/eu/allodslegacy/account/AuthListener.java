package eu.allodslegacy.account;

import akka.actor.typed.ActorSystem;
import akka.stream.ActorAttributes;
import akka.stream.OverflowStrategy;
import akka.stream.Supervision;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Tcp;
import com.typesafe.config.Config;
import eu.allodslegacy.account.authenticator.Authenticator;
import eu.allodslegacy.account.certificate.ServerCertificate;
import eu.allodslegacy.account.db.dao.AccountDataSetDAO;
import eu.allodslegacy.account.flows.AuthFlow;
import eu.allodslegacy.account.flows.DDOSCheckerFlow;
import eu.allodslegacy.account.flows.ShardListFlow;
import eu.allodslegacy.account.flows.ValidationCheckerFlow;
import eu.allodslegacy.io.crypto.CryptoUtils;
import eu.allodslegacy.io.crypto.RSACipher;
import eu.allodslegacy.io.net.Client;
import eu.allodslegacy.io.net.ClientQueue;
import eu.allodslegacy.io.net.version.MsgVersionCheckFlow;
import eu.allodslegacy.io.net.version.NetVersionCheckFlow;
import eu.allodslegacy.io.net.version.Version;
import eu.allodslegacy.io.serialization.CppInSerializable;
import eu.allodslegacy.io.serialization.MsgFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class AuthListener {

    private final Logger log = LoggerFactory.getLogger(AuthListener.class);

    private final Config config;

    private final AccountDataSetDAO accountDataSetDAO;

    public AuthListener(Config config, AccountDataSetDAO accountDataSetDAO) {
        this.config = config;
        this.accountDataSetDAO = accountDataSetDAO;
    }

    public void start(ActorSystem<Void> actorSystem) throws Exception {

        final String host = this.config.getString("host");
        final int port = this.config.getInt("port");

        final RSAPrivateKey serverPrivateKey = CryptoUtils.loadPrivateKey("rsa.server.modulus.bin", "rsa.server.prvExponent.bin");
        final RSAPublicKey clientPublicKey = CryptoUtils.loadPublicKey("rsa.client.modulus.bin", "rsa.client.pubExponent.bin");
        final ServerCertificate certificate = ServerCertificate.load("certificate.bin");
        final RSACipher serverRSACipher = new RSACipher(SecureRandom.getInstance("SHA1PRNG"), serverPrivateKey, null, clientPublicKey);
        final Authenticator authenticator = Authenticator.create(config.getString("authenticator"), this.accountDataSetDAO);
        final ClientQueue<AccountServerClient> queue = new ClientQueue<>();
        MsgFactory<CppInSerializable> msgFactory = MsgFactory.create("eu.allodslegacy.account.msg", CppInSerializable.class);

        RunnableGraph<CompletionStage<Tcp.ServerBinding>> serverLogic = Tcp.get(actorSystem).bind(host, port)
                .map(connection -> {
                    SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
                    AccountServerClient accountServerClient = new AccountServerClient(connection, actorSystem, secureRandom);
                    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                    keyPairGenerator.initialize(1024, secureRandom);
                    KeyPair keyPair = keyPairGenerator.generateKeyPair();
                    accountServerClient.getRSACipher().setDecryptKey((RSAPrivateKey) keyPair.getPrivate(), (RSAPublicKey) keyPair.getPublic());
                    return accountServerClient;
                })
                .<AccountServerClient>mapAsync(1, client -> client.attachFlow(NetVersionCheckFlow.create(Version.ACCOUNT_SERVER_NET_VERSION)))
                .<AccountServerClient>mapAsync(1, client -> client.attachFlow(MsgVersionCheckFlow.create(Version.ACCOUNT_SERVER_MSG_1_1_04_VERSION, Version.ACCOUNT_MSG_VERSION_OK)))
                .<AccountServerClient>mapAsync(1, client -> client.attachFlow(DDOSCheckerFlow.create(6)))
                .<AccountServerClient>mapAsync(1, client -> client.attachFlow(ValidationCheckerFlow.create(new byte[]{94, 45, 44, 53, 120, 0, 40, 55, 2, 7, 0, 4, 42, 9, 10, 1})))
                .via(queue)
                .<AccountServerClient>mapAsync(1, client -> client.attachFlow(AuthFlow.create(serverRSACipher, client.getRSACipher(), certificate, client.getIp(), authenticator)))
                .<AccountServerClient>mapAsync(1, client -> client.attachFlow(ShardListFlow.create(msgFactory)))
                .delay(Duration.ofSeconds(5), OverflowStrategy.dropHead())
                .to(Sink.foreach(Client::closeConnection))
                .withAttributes(ActorAttributes.withSupervisionStrategy(Supervision.getResumingDecider()));

        serverLogic.run(actorSystem).whenComplete((serverBinding, throwable) ->
                log.info("Auth listener started on host {}, on port {}", serverBinding.localAddress().getAddress(), serverBinding.localAddress().getPort())
        );
    }
}
