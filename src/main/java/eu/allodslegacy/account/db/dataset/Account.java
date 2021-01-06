package eu.allodslegacy.account.db.dataset;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.allodslegacy.AccessLevel;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Account {

    private String login;
    @JsonIgnore
    private String passwordHash;
    @JsonIgnore
    private String salt;
    private String lastShardName = "";
    private AccessLevel accessLevel;
    private AccessLevel currentAccessLevel;
    private AccountStatus status;
    private long flags = 0;
    private long creationTime = -1;
    private String lastIp = "";
    private long lastShardEnter = -1;
    private long lastShardQuit = -1;
    private String lastAvatarName = "";
    private List<Sanction> sanctions;

    public Account() {

    }

    public Account(String login, String passwordHash, String salt, AccessLevel accessLevel) {
        this.login = login;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.accessLevel = accessLevel;
        this.currentAccessLevel = accessLevel;
        this.sanctions = new ArrayList<>();
        this.creationTime = Date.from(Instant.now()).getTime();
        this.status = AccountStatus.INACTIVE;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getLastShardName() {
        return lastShardName;
    }

    public void setLastShardName(String lastShardName) {
        this.lastShardName = lastShardName;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    public AccessLevel getCurrentAccessLevel() {
        return currentAccessLevel;
    }

    public void setCurrentAccessLevel(AccessLevel currentAccessLevel) {
        this.currentAccessLevel = currentAccessLevel;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public long getFlags() {
        return flags;
    }

    public void setFlags(Long flags) {
        this.flags = flags;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public String getLastIp() {
        return lastIp;
    }

    public void setLastIp(String lastIp) {
        this.lastIp = lastIp;
    }

    public long getLastShardEnter() {
        return lastShardEnter;
    }

    public void setLastShardEnter(long lastShardEnter) {
        this.lastShardEnter = lastShardEnter;
    }

    public long getLastShardQuit() {
        return lastShardQuit;
    }

    public void setLastShardQuit(long lastShardQuit) {
        this.lastShardQuit = lastShardQuit;
    }

    public String getLastAvatarName() {
        return lastAvatarName;
    }

    public void setLastAvatarName(String lastAvatarName) {
        this.lastAvatarName = lastAvatarName;
    }

    public List<Sanction> getSanctions() {
        return sanctions;
    }

    public void setSanctions(List<Sanction> sanctions) {
        this.sanctions = sanctions;
    }

    @BsonIgnore
    public boolean isBanned() {
        for (Sanction sanction : this.sanctions) {
            if (sanction.getSanctionType() == Sanction.SanctionType.BAN && sanction.isActive()) {
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
