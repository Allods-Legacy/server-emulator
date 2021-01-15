package eu.allodslegacy.account.msg;

import eu.allodslegacy.io.serialization.*;
import org.jetbrains.annotations.NotNull;

@SerializationId(4)
public class LoginResultMsg implements CppOutSerializable {
    @NotNull
    private String login = "";
    @NotNull
    private String sessionId = "";
    @NotNull
    private String reloginId = "";
    @NotNull
    private LoginResult result;
    @NotNull
    private String debugReason;
    @NotNull
    private String lastShardName = "";
    @NotNull
    private String lastIp = "";
    private long lastShardEnter = -1;
    private long lastShardQuit = -1;
    @NotNull
    private String lastAvatarName = "";
    private long flags;

    public LoginResultMsg(@NotNull LoginResult result, @NotNull String reason) {
        this.result = result;
        this.debugReason = reason;
    }

    public void setLogin(@NotNull String login) {
        this.login = login;
    }

    public void setSessionId(@NotNull String sessionId) {
        this.sessionId = sessionId;
    }

    public void setReloginId(@NotNull String reloginId) {
        this.reloginId = reloginId;
    }

    public void setResult(@NotNull LoginResult result) {
        this.result = result;
    }

    public void setDebugReason(@NotNull String debugReason) {
        this.debugReason = debugReason;
    }

    public void setLastShardName(@NotNull String lastShardName) {
        this.lastShardName = lastShardName;
    }

    public void setLastIp(@NotNull String lastIp) {
        this.lastIp = lastIp;
    }

    public void setLastShardEnter(long lastShardEnter) {
        this.lastShardEnter = lastShardEnter;
    }

    public void setLastShardQuit(long lastShardQuit) {
        this.lastShardQuit = lastShardQuit;
    }

    public void setLastAvatarName(@NotNull String lastAvatarName) {
        this.lastAvatarName = lastAvatarName;
    }

    public void setFlags(long flags) {
        this.flags = flags;
    }

    @Override
    public void writeCpp(SerializationDataOutput out) throws Exception {
        out.writeUTF(this.debugReason);
        out.writeLong(this.flags);
        out.writeUTF(this.lastAvatarName);
        out.writeUTF(this.lastIp);
        out.writeLong(this.lastShardEnter);
        out.writeUTF(this.lastShardName);
        out.writeLong(this.lastShardQuit);
        out.writeUTF(this.login);
        out.writeUTF(this.reloginId);
        out.writeByte(this.result.ordinal());
        out.writeUTF(this.sessionId);
    }

    public enum LoginResult {

        LOGIN_SUCCESS,
        OTHER_CLIENT_IN_GAME,
        WRONG_VERSION,
        SERVER_ERROR,
        UNEXPECTED_DATA,
        WRONG_AUTH_INFO,
        BANNED,
        TIMEOUT,
        SERVER_IS_OVERLOADED,
        ACCOUNT_INACTIVE,
        ACCOUNT_INACTIVE_TEMPORARY,
        WRONG_CERTIFICATE,
        CLIENT_VALIDATION_FAILED;

        public static LoginResult readCpp(SerializationDataInput inputStream) throws Exception {
            int value = inputStream.readByte();
            if (value > 0 && value < LoginResult.values().length) {
                return LoginResult.values()[value];
            } else {
                throw new SerializationException();
            }
        }
    }
}
