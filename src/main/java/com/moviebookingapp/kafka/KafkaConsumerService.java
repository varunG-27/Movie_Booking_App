package com.moviebookingapp.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "movie_topic", groupId = "group_id")
    public void consume(String message) {
        System.out.println("Consumed message: " + message);
        // Process the message (e.g., update ticket availability)
    }
}
