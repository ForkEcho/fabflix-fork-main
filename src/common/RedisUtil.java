package common;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;
import resources.Item;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.naming.InitialContext;

public class RedisUtil {
    private static JedisPool jedisPool;

    public static void init() {
        if (jedisPool != null) {
            return;
        }

        try {
            InitialContext ctx = new InitialContext();
            String address = (String) ctx.lookup("java:comp/env/redis/Address");
            String[] parts = address.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid redis/Address format: " + address);
            }
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(50);
            poolConfig.setMaxIdle(10);
            poolConfig.setMinIdle(2);
            poolConfig.setTestOnBorrow(true);
            jedisPool = new JedisPool(poolConfig, host, port);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Redis connection pool", e);
        }
    }

    public static String get(String key) {
        // Simple Redis get(key) helper.
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

    public static void set(String key, String value, int ttlSeconds) {
        // Simple Redis set(key, value) + expire(ttl) helper.
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, value);
            jedis.expire(key, ttlSeconds);
        }
    }

    public static HashMap<String, Item> getHashMap(String key){
	try (Jedis jedis = jedisPool.getResource()) {
            Gson gson = new Gson();
            Map<String, String> redisHashMap = jedis.hgetAll("items");   
    	    if(redisHashMap.isEmpty()){
	    	return null;
	    }

	    redisHashMap = (HashMap<String, String>) redisHashMap;
	    HashMap<String, Item> results = new HashMap<>();
	    for(Map.Entry<String, String> entry : redisHashMap.entrySet()){
	        Item item = gson.fromJson(entry.getValue(), Item.class);
                results.put(entry.getKey(), item);
	    }
	    return results;	    
	}
    }

    public static void setHashMap(String key, HashMap<String, Item> map, int ttlSeconds){
       try (Jedis jedis = jedisPool.getResource()) {
	    if(jedis.exists("items")){
	    	jedis.del("items");
	    }

	    if(map.isEmpty()){
		return;
	    }

	    Gson gson = new Gson();
	    for(Map.Entry<String, Item> entry : map.entrySet()) {
	        String itemName = entry.getKey();
		String value = gson.toJson(entry.getValue());
		jedis.hset("items", itemName, value);
	    }
	    jedis.expire("items", ttlSeconds);
        }
    }

    public static long increment(String key) {
        // Simple Redis incr(key) helper for counters like accessCount.
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incr(key);
        }
    }
}
