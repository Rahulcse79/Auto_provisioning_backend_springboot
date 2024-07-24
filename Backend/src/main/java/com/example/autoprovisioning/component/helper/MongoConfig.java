package com.example.autoprovisioning.component.helper;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.example.repository")
public class MongoConfig extends AbstractMongoClientConfiguration {
    
    public static String CollectionName = "devices";

    @Override
    protected String getDatabaseName() {
        return "genieacs";
    }

    @Override
    public MongoClient mongoClient() {
        String mongoUri = "mongodb://" + Constants.MongoDB_IP + ":27017/genieacs";
        System.out.println("Connecting to MongoDB using URI: " + mongoUri);
        return MongoClients.create(mongoUri);
    }

    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient(), getDatabaseName());
        System.out.println("MongoTemplate initialized for database: " + getDatabaseName());
        return mongoTemplate;
    }
}
