package online.wangxuan.redis;

import online.wangxuan.util.ApplicationContextHolder;
import org.apache.ibatis.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>将Redis作为二级缓存</p>
 * mybatis的二级缓存可以自动的对数据库查询做缓存，并且可以在更新数据的同时自动的更新缓存。<br>
 *
 * 实现Mybatis二级缓存很简单，只需要新建一个类实现org.apache.ibatis.cache.Cache接口即可<br><br>
 *
 *
 * Created by wangxuan on 2017/6/21.
 */
public class RedisCache implements Cache {

    private static final Logger logger = LoggerFactory.getLogger(RedisCache.class);

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final String id; //cache instance id

    /**
     * 使用redisTemplate，我们不用关心redis连接的释放问题。
     *
     * 这里不能通过autowire的方式引用redisTemplate，因为RedisCache并不是Spring容器里的bean。
     * 所以我们需要手动的去调用容器的getBean方法来拿到这个bean。
     */
    private RedisTemplate redisTemplate;

    private static final long EXPIRE_TIME_IN_MINUTES = 30; // redis过期时间

    /**
     * 自己实现的二级缓存，必须要有一个带id的构造函数，否则会报错
     * @param id
     */
    public RedisCache(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Cache instances require an ID");
        }
        this.id = id;
    }

    /**
     * mybatis缓存操作对象的标识符，一个mapper对应一个mybatis的缓存操作对象
     * @return
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * 将查询结果塞入缓存
     * @param key
     * @param value
     */
    @Override
    public void putObject(Object key, Object value) {
        RedisTemplate redisTemplate = getRedisTemplate();
        ValueOperations opsForValue = redisTemplate.opsForValue();
        opsForValue.set(key, value, EXPIRE_TIME_IN_MINUTES, TimeUnit.MINUTES);
        logger.debug("Put query result to redis");
    }

    /**
     * 从缓存中获取被缓存的查询结果
     * @param key
     * @return
     */
    @Override
    public Object getObject(Object key) {
        RedisTemplate redisTemplate = getRedisTemplate();
        ValueOperations opsForValue = redisTemplate.opsForValue();
        logger.debug("Get cached query result from redis");
        return opsForValue.get(key);
    }

    /**
     * 从缓存中删除对应的key、value 只有在回滚时触发。
     * 一般我们也可以不用实现，具体使用方式请参考：
     * org.apache.ibatis.cache.decorators.TransactionalCache
     * @param key
     * @return
     */
    @Override
    public Object removeObject(Object key) {
        RedisTemplate redisTemplate = getRedisTemplate();
        redisTemplate.delete(key);
        logger.debug("Remove cached query result from redis");
        return null;
    }

    /**
     * 发生更新时，清除缓存。
     */
    @Override
    public void clear() {
        RedisTemplate redisTemplate = getRedisTemplate();

        // java8 lambda
        /*redisTemplate.execute((RedisCallback) connection -> {
            connection.flushDb();
            return null;
        });*/

        redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.flushDb();
                return null;
            }
        });
        logger.debug("Clear all the cached query result from redis");
    }

    /**
     * 可选实现，返回缓存的数量
     * @return
     */
    @Override
    public int getSize() {
        return 0;
    }

    /**
     * 可选实现，用于实现原子性的缓存操作
     * @return
     */
    @Override
    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    private RedisTemplate getRedisTemplate() {
        if (redisTemplate == null) {
            redisTemplate = ApplicationContextHolder.getBean("redisTemplate");
        }
        return redisTemplate;
    }
}
