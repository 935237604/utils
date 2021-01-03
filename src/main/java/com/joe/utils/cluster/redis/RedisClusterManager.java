package com.joe.utils.cluster.redis;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import org.redisson.api.RObject;
import org.redisson.api.RedissonClient;

import com.joe.utils.cluster.ClusterManager;
import com.joe.utils.cluster.Topic;

/**
 * redis实现的分布式锁管理器
 *
 * @author joe
 */
public class RedisClusterManager implements ClusterManager {
    private RedissonClient redissonClient;

    RedisClusterManager(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public boolean exist(String key) {
        return redissonClient.getKeys().isExists(key) > 0;
    }

    @Override
    public Lock getLock(String name) {
        return redissonClient.getLock(name);
    }

    @Override
    public ReadWriteLock getReadWriteLock(String name) {
        return redissonClient.getReadWriteLock(name);
    }

    @Override
    public <V> BlockingDeque<V> getBlockingDeque(String name) {
        return redissonClient.getBlockingDeque(name);
    }

    @Override
    public <V> BlockingQueue<V> getBlockingQueue(String name) {
        return redissonClient.getBlockingQueue(name);
    }

    @Override
    public <V> List<V> getList(String name) {
        return redissonClient.getList(name);
    }

    @Override
    public <K, V> Map<K, V> getMap(String name) {
        return redissonClient.getMap(name);
    }

    @Override
    public <K, V> ConcurrentMap<K, V> getConcurrentMap(String name) {
        return redissonClient.getMap(name);
    }

    @Override
    public <K> Set<K> getSet(String name) {
        return redissonClient.getSet(name);
    }

    @Override
    public <M> Topic<M> getTopic(String name) {
        return new RedisTopic<>(redissonClient.getTopic(name));
    }

    @Override
    public void shutdown() {
        redissonClient.shutdown();
    }

    @Override
    public boolean free(Object obj) {
        if (obj instanceof RObject) {
            RObject rObject = (RObject)obj;
            return rObject.delete();
        } else {
            return false;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        // 不应该依赖该方法进行shutdown！！！
        super.finalize();
        redissonClient.shutdown();
    }
}
