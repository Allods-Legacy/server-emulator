package eu.allodslegacy.account.db.dao;

import com.typesafe.config.Config;

import java.lang.reflect.InvocationTargetException;

public interface DAOFactory {

    static DAOFactory create(Config databaseConfig) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return (DAOFactory) Class.forName(databaseConfig.getString("factory")).getConstructor(Config.class).newInstance(databaseConfig);
    }

    AccountDataSetDAO getAccountDataSetDAO();
}
