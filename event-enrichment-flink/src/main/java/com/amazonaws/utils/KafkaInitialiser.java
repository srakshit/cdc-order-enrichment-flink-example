package com.amazonaws.utils;

import com.amazonaws.pojo.Order;
import com.amazonaws.pojo.Rate;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaInitialiser {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaInitialiser.class);

    private static KafkaSource<Order> createKafkaSourceOrder(ParameterTool applicationProperties, String streamName) {
        return KafkaSource.<Order>builder()
                .setBootstrapServers(applicationProperties.get("bootstrap.servers"))
                .setTopics(applicationProperties.get("raw.event.topic", streamName))
                .setGroupId(applicationProperties.get("order.consumer.group.id", "orders-consumer"))
                .setStartingOffsets(OffsetsInitializer.latest()) // Used when the application starts with no state
                .setValueOnlyDeserializer(new JsonDeserializationSchema<>(Order.class))
                .build();
    }

    private static KafkaSource<Rate> createKafkaSourceRate(ParameterTool applicationProperties, String streamName) {
        return KafkaSource.<Rate>builder()
                .setBootstrapServers(applicationProperties.get("bootstrap.servers"))
                .setTopics(applicationProperties.get("reference.data.topic", streamName))
                .setGroupId("rates-consumer")
                .setStartingOffsets(OffsetsInitializer.earliest()) // Used when the application starts with no state
                .setValueOnlyDeserializer(new JsonDeserializationSchema<>(Rate.class))
                .build();
    }

    public static DataStream<Rate> createRateStream(StreamExecutionEnvironment env, ParameterTool applicationProperties, String streamName) {
        return env.fromSource(createKafkaSourceRate(applicationProperties, streamName),
                WatermarkStrategy.noWatermarks(),
                "Kafka Rate Source");
    }

    public static DataStream<Order> createOrderStream(StreamExecutionEnvironment env, ParameterTool applicationProperties, String streamName) {
        return env.fromSource(createKafkaSourceOrder(applicationProperties, streamName),
                WatermarkStrategy.noWatermarks(),
                "Kafka Order Source");
    }
}
