package eu.allodslegacy.account;

import akka.actor.typed.ActorSystem;
import akka.stream.javadsl.Tcp;
import eu.allodslegacy.io.crypto.RSACipher;
import eu.allodslegacy.io.net.Client;

import java.security.SecureRandom;

public class AccountServerClient extends Client {

    private final RSACipher rsaCipher;

    public AccountServerClient(Tcp.IncomingConnection connection, ActorSystem<Void> actorSystem, SecureRandom secureRandom) {
        super(connection, actorSystem);
        this.rsaCipher = new RSACipher(secureRandom);
    }

    public RSACipher getRSACipher() {
        return this.rsaCipher;
    }
}
