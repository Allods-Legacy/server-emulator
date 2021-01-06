package eu.allodslegacy.account.authenticator;

import eu.allodslegacy.account.db.dataset.Account;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AuthenticationResult {

    @NotNull
    private final ResultCode resultCode;
    @Nullable
    private final Account account;

    public AuthenticationResult(@NotNull ResultCode resultCode, @Nullable Account account) {
        this.resultCode = resultCode;
        this.account = account;
    }

    public @NotNull ResultCode getResultCode() {
        return resultCode;
    }

    public @Nullable Account getAccountDataSet() {
        return account;
    }
}
