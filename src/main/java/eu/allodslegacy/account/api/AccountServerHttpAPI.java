package eu.allodslegacy.account.api;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.*;
import eu.allodslegacy.account.api.request.CreateAccountRequest;
import eu.allodslegacy.account.db.dao.AccountDataSetDAO;

public class AccountServerHttpAPI extends AllDirectives {

    private final AccountDataSetDAO accountDataSetDAO;

    public AccountServerHttpAPI(AccountDataSetDAO accountDataSetDAO) {
        this.accountDataSetDAO = accountDataSetDAO;
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
