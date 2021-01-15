package eu.allodslegacy.account.flows;

import akka.Done;
import akka.stream.Attributes;
import akka.stream.javadsl.Flow;
import akka.stream.stage.AbstractInHandler;
import akka.stream.stage.AbstractOutHandler;
import akka.stream.stage.AsyncCallback;
import akka.stream.stage.GraphStageLogic;
import akka.util.ByteString;
import eu.allodslegacy.account.AccountServerClient;
import eu.allodslegacy.account.authenticator.AuthInfo;
import eu.allodslegacy.account.authenticator.AuthenticationResult;
import eu.allodslegacy.account.authenticator.Authenticator;
import eu.allodslegacy.account.certificate.ServerCertificate;
import eu.allodslegacy.account.db.dataset.Account;
import eu.allodslegacy.account.msg.*;
import eu.allodslegacy.io.crypto.CryptoUtils;
import eu.allodslegacy.io.crypto.RSACipher;
import eu.allodslegacy.io.net.NetGraphStage;
import eu.allodslegacy.io.serialization.CppOutSerializable;
import eu.allodslegacy.io.serialization.CppSerializer;
import org.jetbrains.annotations.NotNull;

import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.CompletionStage;

public class AuthFlow extends NetGraphStage {

    private final AccountServerClient client;
    @NotNull
    private final RSACipher serverCipher;
    @NotNull
    private final RSACipher clientCipher;
    @NotNull
    private final Authenticator authenticator;
    @NotNull
    private final ServerCertificate certificate;
    @NotNull
    private final String clientIp;

    private AuthFlow(AccountServerClient client, @NotNull RSACipher serverCipher, @NotNull RSACipher clientCipher, @NotNull ServerCertificate certificate, @NotNull String clientIp, @NotNull Authenticator authenticator) {
        this.clientCipher = clientCipher;
        this.serverCipher = serverCipher;
        this.authenticator = authenticator;
        this.certificate = certificate;
        this.clientIp = clientIp;
        this.client = client;
    }

    public static Flow<ByteString, ByteString, CompletionStage<Done>> create(AccountServerClient accountServerClient, RSACipher serverCipher, RSACipher clientCipher, ServerCertificate serverCertificate, String clientIp, Authenticator authenticator) {
        return Flow.of(ByteString.class)
                .via(new AuthFlow(accountServerClient, serverCipher, clientCipher, serverCertificate, clientIp, authenticator))
                .watchTermination((notUsed, done) -> done);
    }

    @Override
    public GraphStageLogic createLogic(Attributes inheritedAttributes) {
        return new GraphStageLogic(shape) {

            private DialogState state;
            private AsyncCallback<AuthenticationResult> authCallback;

            {
                setHandler(in, new AbstractInHandler() {

                    @Override
                    public void onPush() throws Exception {
                        RSAEncryptedMsg msg = CppSerializer.deserialize(grab(in), RSAEncryptedMsg.class);
                        if (state == DialogState.LOGIN_IN_PROGRESS) {
                            return;
                        }
                        byte[] decryptedData;
                        byte[] encryptedData;
                        CppOutSerializable response = null;
                        switch (msg.getEncryptionMethod()) {

                            case NOT_ENCRYPTED:
                                break;

                            case SECRET_KEY:
                                decryptedData = serverCipher.decrypt(msg.getData());
                                if (state == DialogState.PUBLIC_KEY) {
                                    RSAPublicKeyMsg clientPublicKeyMsg = CppSerializer.deserialize(decryptedData, RSAPublicKeyMsg.class);
                                    RSAPublicKey clientPublicKey = CryptoUtils.constructPublicKey(clientPublicKeyMsg.getModulus(), clientPublicKeyMsg.getExponent());
                                    clientCipher.setCryptKey(clientPublicKey);
                                    RSAPublicKeyMsg serverPublicKeyMsg = new RSAPublicKeyMsg(
                                            clientCipher.getPublicKey().getModulus().toByteArray(),
                                            clientCipher.getPublicKey().getPublicExponent().toByteArray()
                                    );
                                    encryptedData = serverCipher.encrypt(clientCipher.encrypt(CppSerializer.serialize(serverPublicKeyMsg)));
                                    response = new RSAEncryptedMsg(RSAEncryptedMsg.EncryptionMethod.RANDOM_AND_SECRET_KEY, encryptedData);
                                    state = DialogState.ENCRYPTED_CERTIFICATE;
                                }
                                break;

                            case RANDOM_KEY:
                                decryptedData = clientCipher.decrypt(msg.getData());
                                if (state == DialogState.ENCRYPTED_LOGIN_PASSWORD) {
                                    LoginMsg loginMsg = CppSerializer.deserialize(decryptedData, LoginMsg.class);
                                    AuthInfo authInfo = new AuthInfo(loginMsg.getUserName(), loginMsg.getPassword(), clientIp);
                                    authenticator.authenticate(authInfo).thenAccept(authCallback::invoke);
                                    state = DialogState.LOGIN_IN_PROGRESS;
                                } else if (state == DialogState.ENCRYPTED_CERTIFICATE) {
                                    CertificateRequestMsg certificateRequestMsg = CppSerializer.deserialize(decryptedData, CertificateRequestMsg.class);
                                    byte[] responseCertificate = certificate.sign(certificateRequestMsg.getSeed());
                                    encryptedData = clientCipher.encrypt(CppSerializer.serialize(new CertificateResponse(responseCertificate)));
                                    response = new RSAEncryptedMsg(RSAEncryptedMsg.EncryptionMethod.RANDOM_KEY, encryptedData);
                                    state = DialogState.ENCRYPTED_LOGIN_PASSWORD;
                                }
                                break;
                        }
                        if (response != null) {
                            push(out, ByteString.fromArray(CppSerializer.serializeWithId(response)));
                            return;
                        }
                        if (state != DialogState.LOGIN_IN_PROGRESS) {
                            encryptedData = clientCipher.encrypt(CppSerializer.serialize(new LoginResultMsg(LoginResultMsg.LoginResult.SERVER_ERROR, "")));
                            response = new RSAEncryptedMsg(RSAEncryptedMsg.EncryptionMethod.RANDOM_KEY, encryptedData);
                            push(out, ByteString.fromArray(CppSerializer.serializeWithId(response)));
                            completeStage();
                        }
                    }
                });

                setHandler(out, new AbstractOutHandler() {
                    @Override
                    public void onPull() {
                        pull(in);
                    }
                });
            }

            @Override
            public void preStart() {
                state = DialogState.PUBLIC_KEY;
                authCallback = createAsyncCallback(authenticationResult -> {
                    LoginResultMsg loginResultMsg = new LoginResultMsg(LoginResultMsg.LoginResult.SERVER_ERROR, "");
                    switch (authenticationResult.getResultCode()) {
                        case SUCCESS -> {
                            Account account = authenticationResult.getAccountDataSet();
                            if (account == null) {
                                break;
                            }
                            client.setAccount(account);
                            loginResultMsg.setResult(LoginResultMsg.LoginResult.LOGIN_SUCCESS);
                            loginResultMsg.setLogin(account.getLogin());
                            loginResultMsg.setLastIp(account.getLastIp());
                            loginResultMsg.setLastAvatarName(account.getLastAvatarName());
                            loginResultMsg.setLastIp(account.getLastIp());
                            loginResultMsg.setLastShardEnter(account.getLastShardEnter());
                            loginResultMsg.setLastShardQuit(account.getLastShardQuit());
                            loginResultMsg.setLastShardName(account.getLastShardName());
                            loginResultMsg.setReloginId("a789dlm");
                            loginResultMsg.setSessionId("a7y9edm");
                            loginResultMsg.setFlags(account.getFlags());
                        }
                        case WRONG_AUTH_INFO -> loginResultMsg.setResult(LoginResultMsg.LoginResult.WRONG_AUTH_INFO);
                        case BANNED -> loginResultMsg.setResult(LoginResultMsg.LoginResult.BANNED);
                        case ACCOUNT_INACTIVE -> loginResultMsg.setResult(LoginResultMsg.LoginResult.ACCOUNT_INACTIVE);
                        case ACCOUNT_INACTIVE_TEMPORARY -> loginResultMsg.setResult(LoginResultMsg.LoginResult.ACCOUNT_INACTIVE_TEMPORARY);
                    }
                    byte[] encryptedData = clientCipher.encrypt(CppSerializer.serialize(loginResultMsg));
                    RSAEncryptedMsg result = new RSAEncryptedMsg(RSAEncryptedMsg.EncryptionMethod.RANDOM_KEY, encryptedData);
                    emit(out, ByteString.fromArray(CppSerializer.serializeWithId(result)));
                    completeStage();
                });
            }
        };
    }

    enum DialogState {
        PUBLIC_KEY,
        ENCRYPTED_CERTIFICATE,
        ENCRYPTED_LOGIN_PASSWORD,
        LOGIN_IN_PROGRESS
    }
}
