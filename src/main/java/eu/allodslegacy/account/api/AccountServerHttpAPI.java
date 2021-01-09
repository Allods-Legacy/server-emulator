package eu.allodslegacy.account.api;

import akka.actor.typed.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.*;
import com.typesafe.config.Config;
import eu.allodslegacy.account.api.request.CreateAccountRequest;
import eu.allodslegacy.account.db.dao.AccountDataSetDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountServerHttpAPI extends AllDirectives {

    private final Logger log = LoggerFactory.getLogger(AccountServerHttpAPI.class);

    private final AccountDataSetDAO accountDataSetDAO;
    private final Config config;

    public AccountServerHttpAPI(Config config, AccountDataSetDAO accountDataSetDAO) {
        this.config = config;
        this.accountDataSetDAO = accountDataSetDAO;
    }

    public void start(ActorSystem<Void> actorSystem) {
        final String host = this.config.getString("host");
        final int port = this.config.getInt("port");

        Http.get(actorSystem)
                .newServerAt(host, port)
                .bind(this.createRoute())
                .thenAccept(serverBinding -> log.info("API started on host {}, on port {}", serverBinding.localAddress().getAddress(), serverBinding.localAddress().getPort()));
    }

    public Route createRoute() {

        ExceptionHandler exceptionHandler = ExceptionHandler.newBuilder().matchAny((throwable -> complete(StatusCodes.INTERNAL_SERVER_ERROR, throwable.getMessage()))).build();
        RejectionHandler rejectionHandler = RejectionHandler.newBuilder().handleNotFound(complete("NOT FOUND")).build();


        return concat(
                pathPrefix("accounts", () -> concat(
                        pathEnd(() ->
                                concat(
                                        get(() ->
                                                onSuccess(accountDataSetDAO.readAll(), accounts -> complete(StatusCodes.OK, accounts, Jackson.marshaller()))
                                        ),
                                        post(() ->
                                                entity(
                                                        Jackson.unmarshaller(CreateAccountRequest.class),
                                                        request ->
                                                                onSuccess(accountDataSetDAO.create(request.login, request.password, ""), insertOneResult -> complete(StatusCodes.CREATED))
                                                )
                                        ).seal(rejectionHandler, exceptionHandler)
                                )
                        ),
                        path(PathMatchers.segment(), login ->
                                concat(
                                        get(() -> onSuccess(accountDataSetDAO.readByLogin(login), account ->
                                                        account != null ?
                                                                complete(StatusCodes.OK, account, Jackson.marshaller()) :
                                                                complete(StatusCodes.NOT_FOUND, "Account doesn't exist")
                                                )
                                        )
                                )
                        ))
                )
        );
    }

}
