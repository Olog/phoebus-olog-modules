/*
 * Copyright (C) 2020 European Spallation Source ERIC.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.phoebus.olog.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wraps access to Kafka. Using the singleton pattern it sets up a {@link Producer} and creates
 * a topic named {@link #TOPIC_NAME} if it does not exist. Connection parameters are read from command
 * line or properties file.
 */
public class KafkaProducer {

    private static Logger logger = Logger.getLogger(KafkaProducer.class.getName());
    private static KafkaProducer instance;
    private static final short REPLICATION_FACTOR = 1;
    private static final int PARTITIONS = 1;
    private static final String CLEANUP_POLICY = "cleanup.policy";
    private static final String COMPACT_POLICY = "compact";
    private static final String SEGMENT_TIME = "segment.ms";
    private static final String TIME = "10000";
    private static final String DIRTY_2_CLEAN = "min.cleanable.dirty.ratio";
    private static final String RATIO = "0.01";

    private static final String TOPIC_NAME = "olog-notify";
    private static final String KEY = "new";

    private String bootstrapServers;

    private Producer<String, String> producer;

    private ObjectMapper objectMapper = new ObjectMapper();

    public static KafkaProducer getInstance() {
        if (instance == null) {
            instance = new KafkaProducer();
        }
        return instance;
    }

    /**
     * Creates the topic {@link #TOPIC_NAME} if it does not exist, and instantiates a
     * {@link Producer}.
     */
    private KafkaProducer() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/kafka_integration_config.properties"));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to load configuration.", e);
            return;
        }
        bootstrapServers = System.getProperty("kafka.bootstrap.servers",
                properties.getProperty("kafka.bootstrap.servers"));
        logger.log(Level.INFO, "Using Kafka bootstrap servers: " + bootstrapServers);
        discoverAndCreateTopic();

        producer = createProducer(bootstrapServers);
    }

    /**
     * Sends a {@link LogEntryMessage} through the {@link Producer}.
     *
     * @param logEntryMessage The {@link LogEntryMessage} to embed in the Kafka stream message.
     */
    public void send(LogEntryMessage logEntryMessage) {
        try {
            final ProducerRecord<String, String> record =
                    new ProducerRecord<>(TOPIC_NAME, KEY, objectMapper.writeValueAsString(logEntryMessage));
            producer.send(record, (recordMetadata, exception) -> {
                if (exception != null) {
                    logger.log(Level.SEVERE, "Encountered exception when sending message", exception);
                }
                else{
                    logger.log(Level.INFO, "Kafka message acknowledged");
                }
            });
            logger.log(Level.INFO, "Kafka message sent");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to send Kafka message", e);
        }
    }

    private void discoverAndCreateTopic() {
        final Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        final AdminClient adminClient = AdminClient.create(props);
        try {
            final ListTopicsResult res = adminClient.listTopics();
            final KafkaFuture<Set<String>> topics = res.names();
            final Set<String> topicNames = topics.get();
            if (!topicNames.contains(TOPIC_NAME)) {
                logger.log(Level.INFO, "Could not find topic " + TOPIC_NAME + ", creating it.");
                createTopic(adminClient, TOPIC_NAME);
            } else {
                logger.log(Level.INFO, "Topic " + TOPIC_NAME + " already exists.");
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Unable to list topics or create topic " + TOPIC_NAME, ex);
        }
    }

    private void createTopic(AdminClient adminClient, String topicName) {
        final NewTopic newTopic = new NewTopic(topicName, PARTITIONS, REPLICATION_FACTOR);
        final Map<String, String> configs = new HashMap<>();
        configs.put(SEGMENT_TIME, TIME);
        configs.put(CLEANUP_POLICY, COMPACT_POLICY);
        configs.put(DIRTY_2_CLEAN, RATIO);
        newTopic.configs(configs);

        // Create the new topics in the Kafka server.
        try {
            final CreateTopicsResult res = adminClient.createTopics(Arrays.asList(newTopic));
            final KafkaFuture<Void> future = res.all();
            future.get();
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Attempt to create topics failed", ex);
        }
    }

    private Producer<String, String> createProducer(String bootstrapServers) {
        final Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        // Collect messages for 20ms until sending them out as a batch
        props.put("linger.ms", 20);

        // Write String key, value
        final Serializer<String> serializer = new StringSerializer();

        final Producer<String, String> producer =
                new org.apache.kafka.clients.producer.KafkaProducer<>(props, serializer, serializer);

        return producer;
    }
}
