package com.et.taro.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                buildCache("youbikeStations", Duration.ofMinutes(2), 10),
                buildCache("weather", Duration.ofMinutes(30), 10),
                buildCache("airQuality", Duration.ofMinutes(15), 10),
                buildCache("metroStations", Duration.ofHours(24), 10),
                buildCache("metroTimeTables", Duration.ofHours(24), 10),
                buildCache("metroExits", Duration.ofHours(24), 10),
                buildCache("busRoutes", Duration.ofHours(24), 10)
        ));
        return manager;
    }

    private CaffeineCache buildCache(String name, Duration ttl, long maxSize) {
        return new CaffeineCache(name,
                Caffeine.newBuilder()
                        .expireAfterWrite(ttl)
                        .maximumSize(maxSize)
                        .build());
    }
}
