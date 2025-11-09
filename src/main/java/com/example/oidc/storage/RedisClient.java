package com.example.oidc.storage;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class RedisClient {

    private static final Logger logger = LoggerFactory.getLogger(RedisClient.class);
    private final StringRedisTemplate redisTemplate;

    public RedisClient(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setValue(String key, String value, long expiration, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value, expiration, timeUnit);
        } catch (RedisConnectionFailureException e) {
            logger.error("Failed to connect to Redis server. Unable to store value for key: {}", key, e);
            throw new RuntimeException(
                    "Redis connection failed. Please check if Redis server is running and accessible.", e);
        } catch (Exception e) {
            logger.error("Unexpected error while storing value in Redis for key: {}", key, e);
            throw new RuntimeException("Failed to store value in Redis", e);
        }
    }

    public void setValue(String key, String value) {
        long defaultExpiration = 300; // Default expiration time in seconds
        setValue(key, value, defaultExpiration, TimeUnit.SECONDS);
    }

    public String getValue(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (RedisConnectionFailureException e) {
            logger.error("Failed to connect to Redis server. Unable to retrieve value for key: {}", key, e);
            throw new RuntimeException(
                    "Redis connection failed. Please check if Redis server is running and accessible.", e);
        } catch (Exception e) {
            logger.error("Unexpected error while retrieving value from Redis for key: {}", key, e);
            throw new RuntimeException("Failed to retrieve value from Redis", e);
        }
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (RedisConnectionFailureException e) {
            logger.error("Failed to connect to Redis server. Unable to delete key: {}", key, e);
            throw new RuntimeException(
                    "Redis connection failed. Please check if Redis server is running and accessible.", e);
        } catch (Exception e) {
            logger.error("Unexpected error while deleting value from Redis for key: {}", key, e);
            throw new RuntimeException("Failed to delete value from Redis", e);
        }
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public <T> void setObject(String key, T value, long expiration, TimeUnit timeUnit) {
        try {
            String json = objectMapper.writeValueAsString(value);
            setValue(key, json, expiration, timeUnit);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize object for key: {}", key, e);
            throw new RuntimeException("Failed to serialize object", e);
        }
    }

    // Added overloaded setObject method without expiration parameters
    public <T> void setObject(String key, T value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            setValue(key, json); // Use default expiration
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize object for key: {}", key, e);
            throw new RuntimeException("Failed to serialize object", e);
        }
    }

    public <T> T getObject(String key, Class<T> valueType) {
        try {
            String json = getValue(key);
            if (json == null)
                return null;
            return objectMapper.readValue(json, valueType);
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize object for key: {}", key, e);
            throw new RuntimeException("Failed to deserialize object", e);
        }
    }
}