package eu.allodslegacy.account;

import akka.actor.typed.ActorSystem;
import akka.stream.javadsl.Tcp;
import eu.allodslegacy.account.db.dataset.Account;
import eu.allodslegacy.io.crypto.RSACipher;
import eu.allodslegacy.io.net.Client;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;

public class AccountServerClient extends Client {

    private final RSACipher rsaCipher;
    @Nullable
    private Account account;

    public AccountServerClient(Tcp.IncomingConnection connection, ActorSystem<Void> actorSystem, SecureRandom secureRandom) {
        super(connection, actorSystem);
        this.rsaCipher = new RSACipher(secureRandom);
    }

    public RSACipher getRSACipher() {
        return this.rsaCipher;
    }

    public @Nullable Account getAccount() {
        return account;
    }

    public void setAccount(@NotNull Account account) {
        this.account = account;
    }
}
