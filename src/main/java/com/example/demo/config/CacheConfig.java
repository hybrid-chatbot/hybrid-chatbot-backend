// src/main/java/com/example/demo/config/CacheConfig.java
package com.example.demo.config;

import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig implements CachingConfigurer {

    @Override
    public CacheErrorHandler errorHandler() {
        // 캐시 에러가 발생했을 때, 우리가 만든 CustomCacheErrorHandler를 사용하도록 지정합니다.
        return new CustomCacheErrorHandler();
    }
}