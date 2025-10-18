package com.playground.gamification_manager.game.service.impl.leaderboard;

import com.playground.gamification_manager.client.rest.user.UserAlias;
import com.playground.gamification_manager.client.rest.user.UserClient;
import com.playground.gamification_manager.game.service.interfaces.AliasService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AliasServiceImpl implements AliasService {

    private final UserClient userClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AliasCacheConfiguration aliasCacheConfiguration;

    @Override
    public Map<String, String> getAlias(Set<UUID> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }

        // 1. Get all possible aliases from the cache
        var cachedAliases = getAliasesFromCache(userIds);

        // 2. Determine which userIds were not found in the cache
        var missingUserIds = userIds.stream()
                .filter(id -> !cachedAliases.containsKey(id.toString()))
                .collect(Collectors.toSet());

        // 3. If there are missing aliases, fetch them from the user-manager
        if (!missingUserIds.isEmpty()) {
            var fetchedAliases = getAliasesFromUserManager(missingUserIds);

            // 4. Add the newly fetched aliases to the cache and the result map
            if (!fetchedAliases.isEmpty()) {
                setAliasesToCache(fetchedAliases);
                cachedAliases.putAll(fetchedAliases);
            }
        }

        // 5. Return the combined map
        return cachedAliases;
    }

    private Map<String, String> getAliasesFromCache(Set<UUID> userIds) {
        var hashOps = redisTemplate.opsForHash();
        var stringUserIds = userIds.stream().map(UUID::toString).toList();
        // Create a Collection<Object> to satisfy the multiGet method signature
        Collection<Object> hashKeys = new ArrayList<>(stringUserIds);

        // Use HMGET to fetch all aliases in one command
        var cachedValues = hashOps.multiGet(aliasCacheConfiguration.getKey(), hashKeys);

        var foundAliases = new HashMap<String, String>();
        for (int i = 0; i < stringUserIds.size(); i++) {
            if (cachedValues.get(i) != null) {
                foundAliases.put(stringUserIds.get(i), cachedValues.get(i).toString());
            }
        }
        return foundAliases;
    }

    private void setAliasesToCache(Map<String, String> aliasMap) {
        var hashOps = redisTemplate.opsForHash();
        // Use HMSET (via putAll) to save all aliases in one command
        hashOps.putAll(aliasCacheConfiguration.getKey(), aliasMap);
        // Set an expiration on the cache to prevent stale data
        redisTemplate.expire(aliasCacheConfiguration.getKey(), aliasCacheConfiguration.getDuration());
    }

    private Map<String, String> getAliasesFromUserManager(Set<UUID> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        try {
            // The UserClient now returns a List<UserAlias>
            return userClient.getUserAlias(userIds)
                    .stream()
                    .collect(Collectors.toMap(UserAlias::getId, UserAlias::getAlias));
        } catch (Exception e) {
            log.error("Failed to get user aliases from user-manager for {} users.", userIds.size(), e);
            // Return an empty map on failure to ensure resilience
            return Collections.emptyMap();
        }
    }
}