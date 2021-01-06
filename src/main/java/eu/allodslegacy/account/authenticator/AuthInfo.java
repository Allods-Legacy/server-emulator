package eu.allodslegacy.account.authenticator;

import org.jetbrains.annotations.NotNull;

public class AuthInfo {

    @NotNull
    private final String login;
    @NotNull
    private final String password;
    @NotNull
    private final String ip;

    public AuthInfo(@NotNull String login, @NotNull String password, @NotNull String ip) {
        this.login = login;
        this.password = password;
        this.ip = ip;
    }

    public @NotNull String getLogin() {
        return login;
    }

    public @NotNull String getPassword() {
        return password;
    }

    public @NotNull String getIp() {
        return ip;
    }
}
