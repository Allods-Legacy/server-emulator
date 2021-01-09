package eu.allodslegacy.account;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.http.javadsl.Http;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import eu.allodslegacy.account.api.AccountServerHttpAPI;
import eu.allodslegacy.account.db.dao.DAOFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountServer {

    private static final Logger log = LoggerFactory.getLogger(AccountServer.class);

    public static void main(String[] args) throws Exception {
        final ActorSystem<Void> system = ActorSystem.create(Behaviors.empty(), "accountServer");
        log.info("Starting account server ...");
        Config config = ConfigFactory.load().getConfig("account-server");

        DAOFactory factory = DAOFactory.create(config.getConfig("database"));
        AuthListener authListener = new AuthListener(config.getConfig("auth-listener"), factory.getAccountDataSetDAO());
        Http http = Http.get(system);
        AccountServerHttpAPI app = new AccountServerHttpAPI(factory.getAccountDataSetDAO());
        authListener.start(system);
        http.newServerAt("localhost", 8080).bind(app.createRoute()).whenComplete((done, err) -> log.info("API listening on host: {} on port: {}", done.localAddress().getAddress(), done.localAddress().getPort()));
    }

}
