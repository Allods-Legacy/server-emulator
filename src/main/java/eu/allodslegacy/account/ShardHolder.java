package eu.allodslegacy.account;


import eu.allodslegacy.FrontEndInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ShardHolder {

    private final Map<String, FrontEndInfo> shards;

    public ShardHolder() {
        this.shards = new HashMap<>();
    }

    @Nullable
    public FrontEndInfo getShard(String name) {
        return this.shards.get(name);
    }

    @NotNull
    public FrontEndInfo[] getShards() {
        return this.shards.values().toArray(FrontEndInfo[]::new);
    }

    public void addShard(FrontEndInfo frontEndInfo) {
        this.shards.put(frontEndInfo.getShardName(), frontEndInfo);
    }

    public void removeShard(String name) {
        this.shards.remove(name);
    }
}
