/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.svv;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;
import java.util.Random;

public class TestProducer {

    public static void main(String[] args) throws InterruptedException {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9091,localhost:9092");
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 10000);

        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
        Callback callback = new DummyCallback();

        boolean running = true;

        while (running) {
            String msg = generateRandomString(15);

            if ("end".equals(msg)) {
                running = false;
            } else {
                ProducerRecord<String, String> record = new ProducerRecord<>("test1", msg, msg);
                producer.send(record, callback);
            }
            Thread.sleep(100);
        }

        producer.close();

    }

    private static String generateRandomString(int length) {
        String source = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rand = new Random();
        char[] chars = new char[length];
        for (int i = 0; i < chars.length; ++i) {
            chars[i] = source.charAt(rand.nextInt(source.length()));
        }
        return new String(chars);
    }

    static class DummyCallback implements Callback {

        @Override
        public void onCompletion(RecordMetadata metadata, Exception exception) {
            if (exception != null) {
                System.out.println("Error while producing message to topic :$recordMetadata");
                exception.printStackTrace();
            } else {
                String message = String.format("sent message to topic:%s partition:%s  offset:%s", metadata.topic(), metadata.partition(), metadata.offset());
                System.out.println(message);
            }
        }
    }
}
