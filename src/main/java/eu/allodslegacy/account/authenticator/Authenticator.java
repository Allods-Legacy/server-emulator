package eu.allodslegacy.account.authenticator;

import eu.allodslegacy.account.db.dao.AccountDataSetDAO;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletionStage;

public interface Authenticator {

    static Authenticator create(String authenticatorClass, AccountDataSetDAO accountDataSetDAO) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return (Authenticator) Class.forName(authenticatorClass).getConstructor(AccountDataSetDAO.class).newInstance(accountDataSetDAO);
    }

    CompletionStage<AuthenticationResult> authenticate(AuthInfo authInfo);
}
