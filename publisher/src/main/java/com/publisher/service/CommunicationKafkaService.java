package com.publisher.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@AllArgsConstructor
public class CommunicationKafkaService {

    private final Map<String, CompletableFuture<Object>> storageResponse = new ConcurrentHashMap<>();
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "InTopic", groupId = "publisher")
    public void listen(ConsumerRecord<String, Object> request) {
        String correlationId = request.key();
        CompletableFuture<Object> future = storageResponse.remove(correlationId);
        if (future != null) {
            future.complete(request.value());
        } else {
            log.warn("Received response for unknown correlationId: {}", correlationId);
        }
    }

    public CompletableFuture<Object> send(Map<String, Object> reactionRequest, RequestMethod requestMethod) {
        String correlationId = UUID.randomUUID().toString();
        Map<String, Object> requestBody = reactionRequest != null ? new HashMap<>(reactionRequest) : new HashMap<>();
        log.error("CommunicationKafkaService. Request body: {}, key: {}, request method: {}, host: {}", requestBody, correlationId, requestMethod, 24110);
        requestBody.put("requestMethod", requestMethod);
        CompletableFuture<Object> responseFuture = new CompletableFuture<>()
                .orTimeout(1800, TimeUnit.MILLISECONDS)
                .exceptionally(ex -> {
                    storageResponse.remove(correlationId);
                    return null;
                });
        storageResponse.put(correlationId, responseFuture);
        kafkaTemplate.send("OutTopic", correlationId, requestBody)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        storageResponse.remove(correlationId);
                        responseFuture.completeExceptionally(ex);
                        log.error("Failed to send message, correlationId: {}", correlationId, ex);
                    } else {
                        log.info("Message sent successfully, correlationId: {}, {}", correlationId, result);
                    }
                });
        return responseFuture;
    }
}
