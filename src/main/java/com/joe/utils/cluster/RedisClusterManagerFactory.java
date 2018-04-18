package com.joe.utils.cluster;

import com.joe.utils.common.BeanUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

/**
 * RedisClusterManager工厂
 *
 * @author joe
 * @version 2018.04.18 11:52
 */
public class RedisClusterManagerFactory {
    // 管理redis连接
    private static Map<RedisBaseConfig, RedisClusterManager> cache = new HashMap<>();
    private static final Object lock = new Object();


    /**
     * 从缓存获取redis实现的分布式管理器
     *
     * @param host redis的主机地址，例如192.168.1.100
     * @param port redis的端口，例如8080
     * @return redis实现的分布式锁管理器
     * @throws MalformedURLException MalformedURLException
     */
    public static RedisClusterManager getInstance(String host, int port) throws MalformedURLException {
        return getInstance(host, port, null);
    }

    /**
     * 从缓存获取redis实现的分布式管理器
     *
     * @param host     redis的主机地址，例如192.168.1.100
     * @param port     redis的端口，例如8080
     * @param password 密码
     * @return redis实现的分布式锁管理器
     * @throws MalformedURLException MalformedURLException
     */
    public static RedisClusterManager getInstance(String host, int port, String password) throws MalformedURLException {
        return getInstance(buildRedisConfig(host, port, password));
    }

    /**
     * 从缓存获取redis实现的分布式管理器
     *
     * @param redisBaseConfig redis配置
     * @return 分布式管理器
     */
    public static RedisClusterManager getInstance(RedisBaseConfig redisBaseConfig) {
        if (!cache.containsKey(redisBaseConfig)) {
            synchronized (lock) {
                if (!cache.containsKey(redisBaseConfig)) {
                    cache.put(redisBaseConfig, newInstance(redisBaseConfig));
                }
            }
        }

        return cache.get(redisBaseConfig);
    }

    /**
     * 创建一个新的redis实现的分布式管理器
     *
     * @param host redis的主机地址，例如192.168.1.100
     * @param port redis的端口，例如8080
     * @return redis实现的分布式锁管理器
     * @throws MalformedURLException MalformedURLException
     */
    public static RedisClusterManager newInstance(String host, int port) throws MalformedURLException {
        return newInstance(host, port, null);
    }

    /**
     * 创建一个新的redis实现的分布式管理器
     *
     * @param host     redis的主机地址，例如192.168.1.100
     * @param port     redis的端口，例如8080
     * @param password 密码
     * @return redis实现的分布式锁管理器
     * @throws MalformedURLException MalformedURLException
     */
    public static RedisClusterManager newInstance(String host, int port, String password) throws MalformedURLException {
        return newInstance(buildRedisConfig(host, port, password));
    }

    /**
     * 创建一个新的redis实现的分布式管理器
     *
     * @param redisBaseConfig redis配置
     * @return 分布式管理器
     */
    public static RedisClusterManager newInstance(RedisBaseConfig redisBaseConfig) {
        return new RedisClusterManager(buildRedissonClient(redisBaseConfig));
    }

    /**
     * 根据host、port、password构建一个redis单机配置文件
     *
     * @param host     redis host
     * @param port     redis port
     * @param password redis password
     * @return redis 单机配置文件
     * @throws MalformedURLException MalformedURLException
     */
    public static RedisSingleServerConfig buildRedisConfig(String host, int port, String password) throws
            MalformedURLException {
        RedisSingleServerConfig config = new RedisSingleServerConfig();
        config.setAddress(host + ":" + port);
        config.setPassword(password);
        return config;
    }

    /**
     * 根据配置构建一个RedissonClient
     *
     * @param redisBaseConfig 配置文件
     * @return 构建的RedissonClient
     */
    private static RedissonClient buildRedissonClient(RedisBaseConfig redisBaseConfig) {
        Config config = new Config();

        if (redisBaseConfig instanceof RedisSingleServerConfig) {
            RedisSingleServerConfig redisSingleServerConfig = (RedisSingleServerConfig) redisBaseConfig;
            SingleServerConfig singleServerConfig = config.useSingleServer();
            BeanUtils.copy(singleServerConfig, redisSingleServerConfig);
            //该字段write方法和get方法类型不一致，需要手动处理
            singleServerConfig.setAddress(redisSingleServerConfig.getAddress());
        } else {
            throw new IllegalArgumentException("位置的配置类型：" + redisBaseConfig.getClass());
        }

        RedissonClient client = Redisson.create(config);
        return client;
    }
}
