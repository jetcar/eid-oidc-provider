package com.example.oidc.storage;

import java.util.concurrent.TimeUnit;

/**
 * Interface for Redis client operations.
 * Provides methods for storing and retrieving values and objects in Redis.
 */
public interface IRedisClient {

    /**
     * Stores a string value in Redis with expiration.
     *
     * @param key        the key to store the value under
     * @param value      the string value to store
     * @param expiration the expiration time
     * @param timeUnit   the time unit for expiration
     */
    void setValue(String key, String value, long expiration, TimeUnit timeUnit);

    /**
     * Stores a string value in Redis with default expiration (300 seconds).
     *
     * @param key   the key to store the value under
     * @param value the string value to store
     */
    void setValue(String key, String value);

    /**
     * Retrieves a string value from Redis.
     *
     * @param key the key to retrieve the value for
     * @return the string value or null if not found
     */
    String getValue(String key);

    /**
     * Deletes a key from Redis.
     *
     * @param key the key to delete
     */
    void delete(String key);

    /**
     * Stores an object in Redis with expiration by serializing it to JSON.
     *
     * @param <T>        the type of the object
     * @param key        the key to store the object under
     * @param value      the object to store
     * @param expiration the expiration time
     * @param timeUnit   the time unit for expiration
     */
    <T> void setObject(String key, T value, long expiration, TimeUnit timeUnit);

    /**
     * Stores an object in Redis with default expiration by serializing it to JSON.
     *
     * @param <T>   the type of the object
     * @param key   the key to store the object under
     * @param value the object to store
     */
    <T> void setObject(String key, T value);

    /**
     * Retrieves an object from Redis by deserializing it from JSON.
     *
     * @param <T>       the type of the object
     * @param key       the key to retrieve the object for
     * @param valueType the class type of the object
     * @return the deserialized object or null if not found
     */
    <T> T getObject(String key, Class<T> valueType);
}
