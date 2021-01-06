package eu.allodslegacy.account.db.dao.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.typesafe.config.Config;
import eu.allodslegacy.account.db.dao.AccountDataSetDAO;
import eu.allodslegacy.account.db.dao.DAOFactory;
import eu.allodslegacy.account.db.dataset.Account;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

public class MongodbDAOFactory implements DAOFactory {

    private final MongoClient mongoClient;

    public MongodbDAOFactory(Config config) {
        ConnectionString connectionString = new ConnectionString(config.getString("connection-string"));
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .codecRegistry(codecRegistry)
                .build();
        this.mongoClient = MongoClients.create(clientSettings);
    }

    @Override
    public AccountDataSetDAO getAccountDataSetDAO() {
        return new AccountDataSetMongodbDAO(this.mongoClient.getDatabase("account").getCollection("accounts", Account.class));
    }
}
