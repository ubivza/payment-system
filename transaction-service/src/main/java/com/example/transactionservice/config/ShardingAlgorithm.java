package com.example.transactionservice.config;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.UUID;

public class ShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>> {

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Comparable<?>> shardingValue) {
        Object rawValue = shardingValue.getValue();

        UUID userId;
        if (rawValue instanceof UUID) {
            userId = (UUID) rawValue;
        } else {
            userId = UUID.fromString(rawValue.toString());
        }

        long hash = userId.getMostSignificantBits() ^ userId.getLeastSignificantBits();
        int suffix = Math.floorMod(hash, 2);

        for (String targetName : availableTargetNames) {
            if (targetName.endsWith(String.valueOf(suffix))) {
                return targetName;
            }
        }
        throw new IllegalArgumentException("No target found for userId=" + userId);
    }

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<Comparable<?>> shardingValue) {
        return availableTargetNames;
    }

    @Override
    public String getType() {
        return "UUID_HASH";
    }
}
