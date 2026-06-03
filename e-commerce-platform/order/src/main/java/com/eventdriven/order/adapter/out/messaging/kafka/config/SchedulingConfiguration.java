package com.eventdriven.order.adapter.out.messaging.kafka.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConditionalOnProperty(value = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
@Configuration
@EnableScheduling
class SchedulingConfiguration {
}
