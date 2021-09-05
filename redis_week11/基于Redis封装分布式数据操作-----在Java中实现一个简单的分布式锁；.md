#### 基于Redis封装分布式数据操作-----在Java中实现一个简单的分布式锁

##### 实现思路

    大致是使用redis分布式锁，对库存操作进行保护

    每当对库存进行操作时，尝试获取redis锁后进行后续操作

##### Redis分布式锁

- 加锁：在Redis的2.6.12及以后中,使用 set key value [NX] [EX] 命令，达到lua脚本的原子性操作

- 释放锁：使用lua脚本进行解锁

- 实现代码如下：

  ```java
  public class RedisLock {
  
      private JedisPool jedisPool = new JedisPool();
  
      /**
       * 进行加锁
       * @param lockValue
       * @param seconds
       * @return
       */
      public boolean lock(String lockValue, int seconds) {
          try(Jedis jedis = jedisPool.getResource()) {
              return "OK".equals(jedis.set(lockValue, lockValue, "NX", "EX", seconds));
          }
      }
  
      /**
       * 释放锁
       * @param lock
       * @return
       */
      public boolean release(String lock) {
          String luaScript = "if redis.call('get',KEYS[1]) == ARGV[1] then " + "return redis.call('del',KEYS[1]) else return 0 end";
          try(Jedis jedis = jedisPool.getResource()) {
              return jedis.eval(luaScript, Collections.singletonList(lock), Collections.singletonList(lock)).equals(1L);
          }
      }
  }
  ```

  

