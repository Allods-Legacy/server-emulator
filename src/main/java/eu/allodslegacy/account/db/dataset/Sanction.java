package eu.allodslegacy.account.db.dataset;

import org.bson.codecs.pojo.annotations.BsonIgnore;

import java.time.Instant;
import java.util.Date;

public class Sanction {

    private SanctionType sanctionType;
    private long setTime;
    private long expireTime;
    private String reason;
    private String gmName;

    public SanctionType getSanctionType() {
        return sanctionType;
    }

    public void setSanctionType(SanctionType sanctionType) {
        this.sanctionType = sanctionType;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getGmName() {
        return gmName;
    }

    public void setGmName(String gmName) {
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
