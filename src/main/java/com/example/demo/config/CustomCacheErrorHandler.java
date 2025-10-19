// src/main/java/com/example/demo/config/CustomCacheErrorHandler.java
package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.lang.NonNull;

@Slf4j
public class CustomCacheErrorHandler implements CacheErrorHandler {

    // 아래의 모든 메서드는 캐시 관련 에러가 발생했을 때 호출됩니다.
    // 우리는 이 메서드들이 아무런 예외(Exception)도 던지지 않도록 하여,
    // 에러가 발생해도 서비스가 중단되지 않도록 만듭니다.

    @Override
    public void handleCacheGetError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key) {
        log.warn("캐시에서 값을 가져오는 중 오류가 발생했습니다. Key: {}", key, exception);
    }

    @Override
    public void handleCachePutError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key, Object value) {
        log.warn("캐시에 값을 쓰는 중 오류가 발생했습니다. Key: {}", key, exception);
    }

    @Override
    public void handleCacheEvictError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key) {
        log.warn("캐시에서 값을 삭제하는 중 오류가 발생했습니다. Key: {}", key, exception);
    }

    @Override
    public void handleCacheClearError(@NonNull RuntimeException exception, @NonNull Cache cache) {
        log.warn("캐시를 비우는 중 오류가 발생했습니다.", exception);
    }
}