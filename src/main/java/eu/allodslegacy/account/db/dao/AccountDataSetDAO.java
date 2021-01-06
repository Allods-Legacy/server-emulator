package eu.allodslegacy.account.db.dao;

import com.mongodb.client.result.InsertOneResult;
import eu.allodslegacy.account.db.dataset.Account;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface AccountDataSetDAO {

    CompletionStage<Account> readByLogin(String login);

    CompletionStage<List<Account>> readAll();

    CompletionStage<InsertOneResult> create(String login, String passwordHash, String salt);

}
