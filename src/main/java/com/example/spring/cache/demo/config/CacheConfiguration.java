package com.example.spring.cache.demo.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Objects;

@Slf4j
@Configuration
@EnableCaching
public class CacheConfiguration extends CachingConfigurerSupport {

    @Bean
    public EhCacheManagerFactoryBean ehCacheManagerFactory() {
        EhCacheManagerFactoryBean ehCacheBean = new EhCacheManagerFactoryBean();
        ehCacheBean.setConfigLocation(new ClassPathResource("ehcache.xml"));
        ehCacheBean.setShared(true);
        return ehCacheBean;
    }

    @Bean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public CacheManager cacheManager() {
        return new EhCacheCacheManager(Objects.requireNonNull(ehCacheManagerFactory().getObject()));
    }

    @Bean
    public CacheManager redisCacheManager() {
        return redisCacheManager(connectionFactory());
    }

    @Bean
    public RedisConnectionFactory connectionFactory() {
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                //.commandTimeout(Duration.ofSeconds(30))
                .shutdownTimeout(Duration.ZERO)
                .build();

        return new LettuceConnectionFactory(new
                RedisStandaloneConfiguration("localhost", 6379), clientConfig);
    }

    public RedisCacheManager redisCacheManager(final RedisConnectionFactory factory) {
        // set ttl for redis cache
        Duration expiration = Duration.ofSeconds(20);

        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager
                .builder(factory)
                .cacheDefaults(RedisCacheConfiguration
                        .defaultCacheConfig()
                        .entryTtl(expiration));
        return builder.build();
    }

    @Bean
    public CacheManager noOpCacheManager() {
        return new NoOpCacheManager();
    }

    public HazelcastInstance hazelCastInstance() {
        Config config = new Config().setClusterName("demo-service")
                // set ttl for hazelcast cache
                .addMapConfig(new MapConfig("countries").setTimeToLiveSeconds(40));
        config.getNetworkConfig()
                .getJoin()
                .getMulticastConfig()
                .setEnabled(true);
        Hazelcast.newHazelcastInstance(config);
        return Hazelcast.newHazelcastInstance(config);
    }

    @Bean
    public CacheManager hazelCastCacheManager() {
        return new HazelcastCacheManager(hazelCastInstance());
    }

    @Bean
    public CacheResolver cacheResolver() {
        // return new MultipleCacheResolver(operations(), redisCacheManager(), noOpCacheManager());
        return new MultipleCacheResolver(redisCacheManager(), hazelCastCacheManager(), noOpCacheManager());
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {

            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.info("Failure getting from cache: " + cache.getName() + ", exception: " + exception.toString());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.info("Failure putting into cache: " + cache.getName() + ", exception: " + exception.toString());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.info("Failure evicting from cache: " + cache.getName() + ", exception: " + exception.toString());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.info("Failure clearing cache: " + cache.getName() + ", exception: " + exception.toString());
            }
        };
    }

//    @Bean
//    public CacheableOperations operations() {
//        CacheableOperations operations = new CacheableOperations(
//                cacheManager(),
//                hazelCastCacheManager(),
//                redisCacheManager(),
//                noOpCacheManager()
//        );
//        operations.init();
//        return operations;
//    }
}
