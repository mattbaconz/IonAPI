package com.ionapi.redis;

import com.ionapi.redis.impl.IonRedisImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Builder for creating IonRedis instances.
 */
public class IonRedisBuilder {

    private String host = "localhost";
    private int port = 6379;
    private String password = null;
    private int database = 0;
    private int timeout = 5000;
    private boolean ssl = false;

    private IonRedisBuilder() {}

    public static @NotNull IonRedisBuilder create() {
        return new IonRedisBuilder();
    }

    public @NotNull IonRedisBuilder host(@NotNull String host) {
        this.host = host;
        return this;
    }

    public @NotNull IonRedisBuilder port(int port) {
        this.port = port;
        return this;
    }

    public @NotNull IonRedisBuilder password(@NotNull String password) {
        this.password = password;
        return this;
    }

    public @NotNull IonRedisBuilder database(int database) {
        this.database = database;
        return this;
    }

    public @NotNull IonRedisBuilder timeout(int timeoutMs) {
        this.timeout = timeoutMs;
        return this;
    }

    public @NotNull IonRedisBuilder ssl(boolean ssl) {
        this.ssl = ssl;
        return this;
    }

    public @NotNull IonRedis build() {
        return new IonRedisImpl(host, port, password, database, timeout, ssl);
    }
}
