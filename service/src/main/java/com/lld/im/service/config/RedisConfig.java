package com.lld.im.service.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * ClassName: RedisConfig
 * Package: com.lld.im.service.config
 * Description:
 *   配置redis以string序列化的方式存到java中来
 * @Author 南极星
 * @Create 2025/8/6 下午3:43
 * Version 1.0
 */
@Configuration  // 标记为配置类，Spring 启动时会加载并执行其中的 @Bean 方法
public class RedisConfig {

    // 注入 Spring 自动配置的 Redis 连接工厂（已包含 Redis 地址、端口等配置）
    @Autowired
    RedisConnectionFactory redisConnectionFactory;

    // 定义一个 RedisTemplate 实例，作为 Spring 容器中的 Bean，供其他地方注入使用
    @Bean
    public RedisTemplate<Object, Object> redisTemplate() {
        // 创建 RedisTemplate 实例（Spring 提供的 Redis 操作模板）
        RedisTemplate<Object, Object> template = new RedisTemplate<>();

        // 设置 Redis 连接工厂（指定用哪个连接池连接 Redis）
        template.setConnectionFactory(redisConnectionFactory);

        // 1. 配置 value 的序列化器（用于序列化对象的值）
        // 使用 Jackson2JsonRedisSerializer：将对象序列化为 JSON 字符串
        Jackson2JsonRedisSerializer serializer = new Jackson2JsonRedisSerializer(Object.class);

        // 配置 JSON 序列化的细节（通过 ObjectMapper）
        ObjectMapper mapper = new ObjectMapper();
        // 允许访问对象的所有属性（包括 private）
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 序列化时保存对象的类型信息（反序列化时能正确还原成原对象类型）
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(mapper);

        // 2. 配置 key 的序列化器（用于序列化键）
        // 使用 StringRedisSerializer：将键序列化为字符串（推荐，因为 Redis 键通常是字符串）
        template.setKeySerializer(new StringRedisSerializer());

        // 3. 配置 value 的序列化器（用上面定义的 JSON 序列化器）
        // 注意：这里代码中有一行被注释，实际生效的是下面的 hash 相关配置
        // template.setValueSerializer(new StringRedisSerializer());  // 这行被注释了，不生效

        // 4. 配置 Hash 类型的键和值的序列化器
        // Hash 是 Redis 中的一种结构（类似 Java 的 Map），需要单独配置其键值的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());  // Hash 的键用字符串序列化
        template.setHashValueSerializer(serializer);  // Hash 的值用 JSON 序列化

        // 初始化模板（应用上面的配置）
        template.afterPropertiesSet();

        return template;
    }
}