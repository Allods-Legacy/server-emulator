package eu.allodslegacy.account.db.dataset;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Date;

public class Sanction {

    @NotNull
    private SanctionType type;
    private long setTime;
    private long expireTime;
    @NotNull
    private String reason;
    @NotNull
    private String gmName;

    @BsonCreator
    public Sanction(@BsonProperty("type") @NotNull SanctionType type, @BsonProperty("setTime") long setTime, @BsonProperty("expireTime") long expireTime, @BsonProperty("reason") @NotNull String reason, @BsonProperty("gmName") @NotNull String gmName) {
        this.type = type;
        this.setTime = setTime;
        this.expireTime = expireTime;
        this.reason = reason;
        this.gmName = gmName;
    }

    public @NotNull SanctionType getType() {
        return type;
    }

    public void setType(@NotNull SanctionType type) {
        this.type = type;
    }

    public long getSetTime() {
        return setTime;
    }

    public void setSetTime(long setTime) {
        this.setTime = setTime;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public @NotNull String getReason() {
        return reason;
    }

    public void setReason(@NotNull String reason) {
        this.reason = reason;
    }

    public @NotNull String getGmName() {
        return gmName;
    }

    public void setGmName(@NotNull String gmName) {
        this.gmName = gmName;
    }

    @BsonIgnore
    public boolean isActive() {
        return expireTime - Date.from(Instant.now()).getTime() > 0;
    }

    public enum SanctionType {
        BAN,
        MUTE
    }
}
