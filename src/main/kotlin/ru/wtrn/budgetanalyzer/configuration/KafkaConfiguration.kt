package ru.wtrn.budgetanalyzer.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class KafkaConfiguration(
    private val objectMapper: ObjectMapper
) {
    @Bean
    fun kafkaProducerFactory(properties: KafkaProperties): DefaultKafkaProducerFactory<out Any, Any> =
        DefaultKafkaProducerFactory<Any, Any>(properties.buildProducerProperties()).apply {
            this.setValueSerializer(JsonSerializer(objectMapper))
        }
}
