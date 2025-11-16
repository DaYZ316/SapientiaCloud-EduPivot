package com.dayz.sapientiacloud_edupivot.celestial_hub.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingOptions;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPooled;

@Configuration
public class VectorConfig {
    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Value("${spring.ai.dashscope.embedding.options.model:text-embedding-v1}")
    private String embeddingModel;

    @Value("${spring.ai.vectorstore.redis.index-name:knowledge-vector-index}")
    private String indexName;

    @Value("${spring.ai.vectorstore.redis.prefix:vector}")
    private String prefix;

    @Value("${spring.ai.vectorstore.redis.initialize-schema:true}")
    private Boolean initializeSchema;

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:zhaosheng123}")
    private String redisPassword;

    @Bean
    public EmbeddingModel embeddingModel() {
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(apiKey)
                .build();

        DashScopeEmbeddingOptions options = DashScopeEmbeddingOptions.builder()
                .withModel(embeddingModel)
                .build();

        return new DashScopeEmbeddingModel(dashScopeApi, MetadataMode.EMBED, options);
    }

    @Bean
    public JedisPooled jedisPooled() {
        DefaultJedisClientConfig.Builder configBuilder = DefaultJedisClientConfig.builder();
        if (redisPassword != null && !redisPassword.isEmpty()) {
            configBuilder.password(redisPassword);
        }
        configBuilder.database(1);
        return new JedisPooled(new HostAndPort(redisHost, redisPort), configBuilder.build());
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel, JedisPooled jedisPooled) {
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName(indexName)
                .prefix(prefix)
                .initializeSchema(initializeSchema)
                .build();
    }
}

