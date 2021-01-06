package eu.allodslegacy.account.authenticator;

import eu.allodslegacy.account.db.dao.AccountDataSetDAO;
import eu.allodslegacy.account.db.dataset.Account;

import java.util.concurrent.CompletionStage;

public class LocalAuthenticator implements Authenticator {

    private final AccountDataSetDAO accountDataSetDAO;

    public LocalAuthenticator(AccountDataSetDAO accountDataSetDAO) {
        this.accountDataSetDAO = accountDataSetDAO;
    }

    @Override
    public CompletionStage<AuthenticationResult> authenticate(AuthInfo authInfo) {
        return this.accountDataSetDAO
                .readByLogin(authInfo.getLogin())
                .thenApply((account -> {
                    if (account != null) {
                        if (account.getPasswordHash().equals(authInfo.getPassword())) {
                            if (account.isBanned()) {
                                return new AuthenticationResult(ResultCode.BANNED, account);
                            } else if (account.getStatus() == Account.AccountStatus.INACTIVE) {
                                return new AuthenticationResult(ResultCode.ACCOUNT_INACTIVE, account);
                            } else {
                                return new AuthenticationResult(ResultCode.SUCCESS, account);
                            }
                        }
                    }
                    return new AuthenticationResult(ResultCode.WRONG_AUTH_INFO, null);
                }))
                .exceptionally((exception) -> new AuthenticationResult(ResultCode.ERROR, null));
    }
}
