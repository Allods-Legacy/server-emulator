package eu.allodslegacy.account.db.dao.mongodb;

import com.mongodb.client.result.InsertOneResult;
import com.mongodb.reactivestreams.client.MongoCollection;
import eu.allodslegacy.AccessLevel;
import eu.allodslegacy.account.db.dao.AccountDataSetDAO;
import eu.allodslegacy.account.db.dataset.Account;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static com.mongodb.client.model.Filters.eq;

public class AccountDataSetMongodbDAO implements AccountDataSetDAO {

    private final MongoCollection<Account> accountCollection;

    public AccountDataSetMongodbDAO(MongoCollection<Account> accountCollection) {
        this.accountCollection = accountCollection;
    }

    @Override
    public CompletionStage<Account> readByLogin(String login) {
        CompletableFuture<Account> result = new CompletableFuture<>();
        accountCollection.find(eq("login", login.toLowerCase())).first().subscribe(new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription subscription) {
                subscription.request(1);
            }

            @Override
            public void onNext(Account account) {
                result.complete(account);
            }

            @Override
            public void onError(Throwable throwable) {
                result.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                if (!result.isDone()) {
                    result.complete(null);
                }
            }
        });
        return result;
    }

    @Override
    public CompletionStage<List<Account>> readAll() {
        CompletableFuture<List<Account>> result = new CompletableFuture<>();
        accountCollection.find().subscribe(new Subscriber<>() {
            final List<Account> accounts = new ArrayList<>();
            Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(Account account) {
                accounts.add(account);
                subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {
                result.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                result.complete(accounts);
            }
        });
        return result;
    }

    @Override
    public CompletionStage<InsertOneResult> create(String login, String passwordHash, String salt) {
        CompletableFuture<InsertOneResult> result = new CompletableFuture<>();
        accountCollection.insertOne(new Account(login.toLowerCase(), passwordHash, salt, AccessLevel.USER)).subscribe(new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription subscription) {
                subscription.request(1);
            }

            @Override
            public void onNext(InsertOneResult insertOneResult) {
                result.complete(insertOneResult);
            }

            @Override
            public void onError(Throwable throwable) {
                result.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                result.complete(null);
            }
        });
        return result;
    }
}
