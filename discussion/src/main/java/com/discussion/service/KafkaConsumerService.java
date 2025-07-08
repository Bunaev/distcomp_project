package com.discussion.service;

import com.datastax.oss.driver.api.core.RequestThrottlingException;
import com.discussion.dto.KafkaMessageDTO;
import com.discussion.dto.ReactionRequestDTO;
import com.discussion.dto.ReactionResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@AllArgsConstructor
public class KafkaConsumerService {
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ReactionService reactionService;
    private static final String TOPIC = "InTopic";


    @KafkaListener(topics = "OutTopic", groupId = "discussion")
    public void listen(ConsumerRecord<String, Object> request, Acknowledgment ack) {
        String key = request.key();
        log.error("KafkaConsumerService.Request body: {}, key: {}, host: {}", request.value(), key, 24130);
        KafkaMessageDTO message = objectMapper.convertValue(request.value(), KafkaMessageDTO.class);
        switch (message.getRequestMethod()) {
            case GET -> {
                if (message.getId() != null) {
                    Long id = message.getId();
                    kafkaTemplate.send(TOPIC, key, reactionService.get(id));
                    ack.acknowledge();
                } else {
                    kafkaTemplate.send(TOPIC, key, reactionService.getReactions());
                    ack.acknowledge();
                }
            }
            case PUT -> {
                ReactionRequestDTO reactionRequestDTO = objectMapper.convertValue(message, ReactionRequestDTO.class);
                ReactionResponseDTO response = reactionService.updateReaction(reactionRequestDTO);
                kafkaTemplate.send(TOPIC, key, response);
                ack.acknowledge();
            }
            case DELETE -> {
                Long id = message.getId();
                kafkaTemplate.send(TOPIC, key, reactionService.deleteReaction(id));
                ack.acknowledge();
            }
            case POST -> {
                ReactionRequestDTO reactionRequestDTO = objectMapper.convertValue(message, ReactionRequestDTO.class);
                ReactionResponseDTO response = reactionService.create(reactionRequestDTO);
                kafkaTemplate.send(TOPIC, key, response);
                ack.acknowledge();
            }
            default -> throw new RequestThrottlingException("Request throttled");
        }
    }
}
