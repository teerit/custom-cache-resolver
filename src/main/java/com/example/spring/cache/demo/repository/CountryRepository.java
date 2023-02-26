package com.example.spring.cache.demo.repository;


import com.example.spring.cache.demo.model.Country;
import com.example.spring.cache.demo.model.Province;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CountryRepository {

    @Cacheable(
            value = "countries",
            key = "#code",
            cacheResolver = "cacheResolver")
    public Country findByCountryCode(String code) {
        log.info("---> Loading country with code={}", code);
        return new Country(code);
    }

    @Cacheable(
            value = "provinces",
            key = "#code",
            cacheManager = "hazelCastCacheManager")
    public Province findByProvinceCode(String code) {
        log.info("---> Loading province with code={}", code);
        return new Province(code);
    }

    @CacheEvict(value = "countries", key = "#code", cacheManager = "redisCacheManager")
    public void deleteCode(String code) {
        log.info("---> Deleting country with code={}", code);
    }
}