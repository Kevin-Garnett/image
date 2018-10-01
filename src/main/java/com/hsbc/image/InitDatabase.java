package com.hsbc.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;

import java.util.UUID;

@Configuration
public class InitDatabase {

    private static Logger logger = LoggerFactory.getLogger(InitDatabase.class);

    // Remember should not have 2 same function name = init() here, otherwise spring boot will only run 1 of them
    // So here should not name init(), otherwise will have the same function name as LoadDatabase.init()
    @Bean
    CommandLineRunner initialize(MongoOperations operations){
        return args -> {
            operations.dropCollection(Image.class);
            operations.insert(new Image(UUID.randomUUID().toString(), "Kevin.jpg"));
            operations.insert(new Image(UUID.randomUUID().toString(), "Sherry.jpg"));
            operations.insert(new Image(UUID.randomUUID().toString(), "Gunter.jpg"));

            operations.findAll(Image.class).forEach(image -> {
                logger.info(image.toString());
            });
        };
    }
}
