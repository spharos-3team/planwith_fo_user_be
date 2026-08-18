package com.planwith.user.adapter.out.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnBean(KafkaTemplate.class)
@RequiredArgsConstructor
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(String topic, EventEnvelope<?> envelope) {
        log.info("Publishing event type={} aggregateId={} topic={}",
                envelope.getEventType(), envelope.getAggregateId(), topic);
        kafkaTemplate.send(topic, envelope.getAggregateId(), envelope);
    }
}
