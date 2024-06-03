package com.kafka.samples;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class OrderProducer{

    private final static Logger logger = LoggerFactory.getLogger(OrderProducer.class);
    private static final Properties properties = new Properties();
    private static Properties applicationProperties = new Properties();
    private static int MIN = 10;
    private static int MAX = 200;
    private static int MIN_DELAY = 10;
    private static int MAX_DELAY = 500;
    public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
    public static final String RESET = "\033[0m";  // Text Reset

    public static final String[] currency = {"USD", "EUR","INR","GBP"};

    public static void main(final String[] args) throws IOException {
        Path resourceDirectory = Paths.get("src", "main", "resources");
        String rootPath = resourceDirectory.toFile().getAbsolutePath();
        String appConfigPath = rootPath + "/app.properties";

        applicationProperties.load(new FileInputStream(appConfigPath));

        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, applicationProperties.getProperty("bootstrap_servers"));
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        Order order = new Order();
        ProducerRecord<String, Order> record;
        KafkaProducer<String, Order> producer = new KafkaProducer<>(properties);
        ObjectMapper objectMapper = new ObjectMapper();
        Random random = new Random();

        int batch = Integer.parseInt(applicationProperties.getProperty("batch.size"));
        long sleepTime = Long.parseLong(applicationProperties.getProperty("sleep.time"));
        boolean shouldGenerateLateEvent = Boolean.parseBoolean(applicationProperties.getProperty("generate.late.event"));
        int lateEventCount = 0;
        int lateEventFrequency = Integer.parseInt(applicationProperties.getProperty("late.event.frequency"));
        long timestamp;

        while (true) {
            try {
                for(int i = 0; i < batch; i++) {
                    order.setOrderId(String.valueOf(ThreadLocalRandom.current().nextInt(1, 9999)));
                    order.setCurrency(currency[random.nextInt(currency.length)]);
                    order.setPrice(MIN + random.nextFloat() * (MAX - MIN));
                    timestamp = System.currentTimeMillis();
                    if (shouldGenerateLateEvent && lateEventCount == lateEventFrequency) {
                        timestamp = timestamp - (MIN_DELAY + random.nextInt(MAX_DELAY - MIN_DELAY))* 1000;
                    }
                    order.setOrderTimestamp(timestamp);
                    if (shouldGenerateLateEvent && lateEventCount == lateEventFrequency) {
                        System.out.println(String.format(YELLOW_BOLD + "Generated late event -> %s" + RESET,order));
                    }else {
                        System.out.println(order);
                    }

                    record = new ProducerRecord(applicationProperties.getProperty("topic"), objectMapper.writeValueAsString(order));
                    producer.send(record);
                }

                lateEventCount = lateEventCount >=lateEventFrequency ? 0: lateEventCount + 1;
                Thread.sleep(sleepTime);
                producer.flush();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }
}