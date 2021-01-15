package eu.allodslegacy.account.db.dataset;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.allodslegacy.AccessLevel;
import eu.allodslegacy.io.serialization.json.ObjectIdSerializer;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Account {

    @NotNull
    private final String login;
    @JsonIgnore
    @NotNull
    private final String passwordHash;
    @JsonIgnore
    @NotNull
    private final String salt;
    @NotNull
    private final AccessLevel accessLevel;
    @NotNull
    private final AccessLevel currentAccessLevel;
    @NotNull
    private final AccountStatus status;
    private final long creationTime;
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId id;
    private long flags = 0;
    @NotNull
    private String lastShardName = "";
    @NotNull
    private String lastIp = "";
    private long lastShardEnter = -1;
    private long lastShardQuit = -1;
    @NotNull
    private String lastAvatarName = "";
    @NotNull
    private List<Sanction> sanctions = new ArrayList<>();
    @NotNull
    private List<AccountShardInfo> accountShardInfos = new ArrayList<>();

    @BsonCreator
    public Account(@BsonProperty("id") ObjectId id, @BsonProperty("login") @NotNull String login, @BsonProperty("passwordHash") @NotNull String passwordHash, @BsonProperty("salt") @NotNull String salt, @BsonProperty("lastShardName") @NotNull String lastShardName, @BsonProperty("accessLevel") @NotNull AccessLevel accessLevel, @BsonProperty("currentAccessLevel") @NotNull AccessLevel currentAccessLevel, @BsonProperty("status") @NotNull AccountStatus status, @BsonProperty("flags") long flags, @BsonProperty("creationTime") long creationTime, @BsonProperty("lastIp") @NotNull String lastIp, @BsonProperty("lastShardEnter") long lastShardEnter, @BsonProperty("lastShardQuit") long lastShardQuit, @BsonProperty("lastAvatarName") @NotNull String lastAvatarName, @NotNull @BsonProperty("sanctions") List<Sanction> sanctions, @NotNull @BsonProperty("accountShardInfos") List<AccountShardInfo> accountShardInfos) {
        this.id = id;
        this.login = login;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.lastShardName = lastShardName;
        this.accessLevel = accessLevel;
        this.currentAccessLevel = currentAccessLevel;
        this.status = status;
        this.flags = flags;
        this.creationTime = creationTime;
        this.lastIp = lastIp;
        this.lastShardEnter = lastShardEnter;
        this.lastShardQuit = lastShardQuit;
        this.lastAvatarName = lastAvatarName;
        this.sanctions = sanctions;
        this.accountShardInfos = accountShardInfos;
    }

    public Account(@NotNull String login, @NotNull String passwordHash, @NotNull String salt, @NotNull AccessLevel accessLevel) {
        this.login = login;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.accessLevel = accessLevel;
        this.currentAccessLevel = accessLevel;
        this.creationTime = Date.from(Instant.now()).getTime();
        this.status = AccountStatus.INACTIVE;
    }

    public ObjectId getId() {
        return id;
    }

    public @NotNull String getLogin() {
        return login;
    }

    public @NotNull String getPasswordHash() {
        return passwordHash;
    }

    public @NotNull String getSalt() {
        return salt;
    }

    public @NotNull String getLastShardName() {
        return lastShardName;
    }

    public @NotNull AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public @NotNull AccessLevel getCurrentAccessLevel() {
        return currentAccessLevel;
    }

    public @NotNull AccountStatus getStatus() {
        return status;
    }

    public long getFlags() {
        return flags;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public @NotNull String getLastIp() {
        return lastIp;
    }

    public long getLastShardEnter() {
        return lastShardEnter;
    }

    public long getLastShardQuit() {
        return lastShardQuit;
    }

    public @NotNull String getLastAvatarName() {
        return lastAvatarName;
    }

    public @NotNull List<Sanction> getSanctions() {
        return sanctions;
    }

    public @NotNull List<AccountShardInfo> getAccountShardInfos() {
        return accountShardInfos;
    }

    @BsonIgnore
    public boolean isBanned() {
        for (Sanction sanction : this.sanctions) {
            if (sanction.getType() == Sanction.SanctionType.BAN && sanction.isActive()) {
                return true;
            }
        }
        return false;
    }

    public enum AccountStatus {
        INACTIVE,
        ACTIVE
    }
}
