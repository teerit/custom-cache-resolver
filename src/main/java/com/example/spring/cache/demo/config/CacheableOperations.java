package com.example.spring.cache.demo.config;

import com.example.spring.cache.demo.repository.CountryRepository;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;

import java.util.*;

@Slf4j
public class CacheableOperations {

    private final CacheManager cacheManager;
    private final CacheManager hazelCastCacheManager;
    private final CacheManager redisCacheManager;
    private final CacheManager noOpCacheManager;

    public CacheableOperations(CacheManager cacheManager, CacheManager hazelCastCacheManager, CacheManager redisCacheManager, CacheManager noOpCacheManager) {
        this.hazelCastCacheManager = hazelCastCacheManager;
        this.cacheManager = cacheManager;
        this.redisCacheManager = redisCacheManager;
        this.noOpCacheManager = noOpCacheManager;
    }

    private Map<String, CacheableOperation<?>> opMap;

    @SuppressWarnings("unchecked")
    public void init() {
        List<CacheableOperation<? extends Class>> ops = new ArrayList<>();

        ops.add(new CacheableOperation.Builder(CountryRepository.class)
                .method("findByCountryCode")
                //.cacheManager(cacheManager)
                .cacheManager(hazelCastCacheManager)
                .build());

        postProcessOperations(ops);
    }

    public Cache getRedisCacheManager(String cacheName) {
        return this.redisCacheManager.getCache(cacheName);
    }

    public CacheableOperation<?> get(CacheOperationInvocationContext<?> context) {
        final String queryKey = getOperationKey(context.getTarget().getClass().getName(),
                context.getMethod().getName());
        log.info("Query key: {}", queryKey);

        //DEBUG
        //Cache ehcache = cacheManager.getCache("countries");
        Cache hazelCastCache = hazelCastCacheManager.getCache("countries");
        assert hazelCastCache != null;
        log.info("Is key in Ehcache: {}", !Objects.isNull(hazelCastCache.get("IN")));

        Cache redisCache = redisCacheManager.getCache("countries");
        assert redisCache != null;
        log.info("Is key in RedisCache: {}", !Objects.isNull(redisCache.get("IN")));

        return opMap.get(queryKey);
    }

    private void postProcessOperations(List<CacheableOperation<? extends Class>> ops) {
        Map<String, CacheableOperation<?>> tempMap = new HashMap<>();
        for (CacheableOperation<?> op : ops) {
            for (String methodName : op.getMethodNames()) {
                tempMap.put(getOperationKey(op.getTargetClass().getName(), methodName), op);
            }
        }

        opMap = ImmutableMap.copyOf(tempMap);
    }

    private String getOperationKey(String first, String second) {
        return String.format("%s-%s", first, second);
    }
}
