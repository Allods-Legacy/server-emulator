package eu.allodslegacy.account.flows;

import akka.Done;
import akka.stream.javadsl.Flow;
import akka.util.ByteString;
import eu.allodslegacy.account.LoginResult;
import eu.allodslegacy.account.authenticator.AuthInfo;
import eu.allodslegacy.account.authenticator.Authenticator;
import eu.allodslegacy.account.db.dataset.Account;
import eu.allodslegacy.account.msg.LoginMsg;
import eu.allodslegacy.account.msg.LoginResultMsg;
import eu.allodslegacy.account.msg.RSAEncryptedMsg;
import eu.allodslegacy.io.crypto.RSACipher;
import eu.allodslegacy.io.serialization.CppSerializer;

import java.util.concurrent.CompletionStage;

public final class AuthFlow {

    public static Flow<ByteString, ByteString, CompletionStage<Done>> create(RSACipher clientCipher, String ip, Authenticator authenticator) {
        return Flow.of(ByteString.class)
                .take(1)
                .map(b -> CppSerializer.deserialize(b, RSAEncryptedMsg.class).getData())
                .via(clientCipher.decrypt())
                .map(data -> CppSerializer.deserialize(data, LoginMsg.class))
                .map(loginMsg -> new AuthInfo(loginMsg.getUserName(), loginMsg.getPassword(), ip))
                .mapAsync(1, authenticator::authenticate)
                .map(authResult -> {
                    LoginResultMsg msg = new LoginResultMsg(LoginResult.SERVER_ERROR, "");
                    switch (authResult.getResultCode()) {
                        case SUCCESS -> {
                            Account account = authResult.getAccountDataSet();
                            if (account == null) {
                                throw new Exception("Account null");
                            }
                            msg.setResult(LoginResult.LOGIN_SUCCESS);
                            msg.setLogin(account.getLogin());
                            msg.setLastIp(account.getLastIp());
                            msg.setLastAvatarName(account.getLastAvatarName());
                            msg.setLastIp(account.getLastIp());
                            msg.setLastShardEnter(account.getLastShardEnter());
                            msg.setLastShardQuit(account.getLastShardQuit());
                            msg.setLastShardName(account.getLastShardName());
                            msg.setReloginId("a789dlm");
                            msg.setSessionId("a7y9edm");
                            msg.setFlags(account.getFlags());
                        }
                        case WRONG_AUTH_INFO -> msg.setResult(LoginResult.WRONG_AUTH_INFO);
                        case ACCOUNT_INACTIVE -> msg.setResult(LoginResult.ACCOUNT_INACTIVE);
                        case ACCOUNT_INACTIVE_TEMPORARY -> msg.setResult(LoginResult.ACCOUNT_INACTIVE_TEMPORARY);
                    }
                    return msg;
                })
                .map(CppSerializer::serialize)
                .via(clientCipher.encrypt())
                .map(encryptedData -> new RSAEncryptedMsg(RSAEncryptedMsg.EncryptionMethod.RANDOM_KEY, encryptedData))
                .map(CppSerializer::serializeWithId)
                .map(ByteString::fromArray)
                .watchTermination((notUsed, done) -> done);
    }


    public enum DialogState {
        PUBLIC_KEY,
        ENCRYPTED_CERTIFICATE,
        ENCRYPTED_LOGIN_PASSWORD
    }

}
