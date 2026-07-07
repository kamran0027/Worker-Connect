package com.workerconnect.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
public class KafkaConfig {


    // this will create a kafka time when the spring boot start 
    // ### even if we do not write it the kafka automatically crete topic when we send data to new topic
    // with the help of kafka template


    // @Bean
    // public NewTopic passwordResetTopic(){
    //     return TopicBuilder
    //                     .name("password-rest")
    //                     .partitions(3)
    //                     .replicas(1)
    //                     .build();
    // }


    // Later, if you need more topics, simply add more beans



    // @Bean
    // public NewTopic orderPlacedTopic() {
    //     return TopicBuilder
    //         .name("order-placed")
    //         .partitions(3)
    //         .replicas(1)
    //         .build();
    // }

    

}
