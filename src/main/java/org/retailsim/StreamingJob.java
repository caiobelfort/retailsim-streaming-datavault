/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.retailsim;


import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Properties;

/**
 * Skeleton for a Flink Streaming Job.
 *
 * <p>For a tutorial how to write a Flink streaming application, check the
 * tutorials and examples on the <a href="http://flink.apache.org/docs/stable/">Flink Website</a>.
 *
 * <p>To package your application into a JAR file for execution, run
 * 'mvn clean package' on the command line.
 *
 * <p>If you change the name of the main class (with the public static void main(String[] args))
 * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
 */
public class StreamingJob {

    public static void main(String[] args) throws Exception {
        // set up the streaming execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", "test");


        env.addSource(new FlinkKafkaConsumer<String>("retailsim.products.aisle",
                                                     new SimpleStringSchema(),
                                                     properties).setStartFromEarliest())
           .map(new ObjectMapper()::readTree)
           .map(e -> e.get("payload"))
           .map((MapFunction<JsonNode, AisleCDC>) jsonNode -> {
               AisleCDC output = new AisleCDC();

               JsonNode before = jsonNode.get("before");
               JsonNode after = jsonNode.get("after");

               if (!before.isNull()) {
                   output.before = new Aisle();
                   output.before.id = before.get("id").asInt();
                   output.before.name = before.get("name").asText();
                   output.before.create_at = LocalDateTimeBuilder.fromMicroseconds(before.get("create_at").asLong());
                   output.before.updated_at = LocalDateTimeBuilder.fromMicroseconds(before.get("updated_at").asLong());
               }
               if (!after.isNull()) {
                   output.after = new Aisle();
                   output.after.id = after.get("id").asInt();
                   output.after.name = after.get("name").asText();
                   output.after.create_at = LocalDateTimeBuilder.fromMicroseconds(after.get("create_at").asLong());
                   output.after.updated_at = LocalDateTimeBuilder.fromMicroseconds(after.get("updated_at").asLong());
               }
               output.op = jsonNode.get("op").asText();
               output.timestamp = LocalDateTimeBuilder.fromMilliseconds(jsonNode.get("ts_ms").asLong());

               return output;
           })
           .map(e -> e.after)
           .print();
        // execute program
        env.execute("Flink Streaming Java API Skeleton");
    }

    public static class LocalDateTimeBuilder {
        public static LocalDateTime fromMicroseconds(long microseconds) {
            return fromMilliseconds(microseconds / 1000);
        }

        public static LocalDateTime fromMilliseconds(long millis) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.of("+0"));
        }
    }
}
