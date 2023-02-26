package com.example.spring.cache.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class MultipleCacheResolver implements CacheResolver {

    private final CacheManager redisCacheManager;

    private final CacheManager hazelCastCacheManager;

    private final CacheManager noOpCacheManager;

    public MultipleCacheResolver(CacheManager redisCacheManager, CacheManager hazelCastCacheManager, CacheManager noOpCacheManager) {
        this.redisCacheManager = redisCacheManager;
        this.hazelCastCacheManager = hazelCastCacheManager;
        this.noOpCacheManager = noOpCacheManager;
    }


    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {

//        Collection<Cache> caches = new ArrayList<>();
//        CacheableOperation<?> operation = operations.get(context);
//        if (operation != null) {
//            CacheManager cacheManager = operation.getCacheManager();
//            if (cacheManager != null) {
//                caches = getCaches(cacheManager, context);
//            }
//        }

        //DEBUG
//        Cache redisCache = redisCacheManager.getCache("countries");
//        assert redisCache != null;
//        log.info("Is key in RedisCache: {}", !Objects.isNull(redisCache.get("IN")));

        Cache hazelCastCache = hazelCastCacheManager.getCache("countries");
        assert hazelCastCache != null;
        log.info("Is key in HazelcastCache: {}", !Objects.isNull(hazelCastCache.get("IN")));

        return getCaches(redisCacheManager, hazelCastCacheManager, context);
    }

    private Collection<Cache> getCaches(CacheManager redisCacheManager, CacheManager hazelCastCacheManager,
                                        CacheOperationInvocationContext<?> context) {
        log.info("cache: {}", context.getOperation().getCacheNames());

        List<Cache> cachesRedis = context.getOperation().getCacheNames().stream()
                .map(redisCacheManager::getCache)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Redis Collections: {}", cachesRedis);

        List<Cache> cachesHazel = context.getOperation().getCacheNames().stream()
                .map(hazelCastCacheManager::getCache)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Hazel Collections: {}", cachesHazel);

        return Stream.concat(cachesRedis.stream(), cachesHazel.stream())
                .collect(Collectors.toList());
    }

//    private Collection<Cache> getCaches(CacheManager cacheManager, CacheOperationInvocationContext<?> context) {
//        var collections = context.getOperation().getCacheNames().stream()
//                .map(cacheManager::getCache)
//                .filter(Objects::nonNull)
//                .collect(Collectors.toList());
//
//        // use layering cache -> Redis L2 cache
//        collections.add(operations.getRedisCacheManager(null));
//        return collections;
//    }
}
