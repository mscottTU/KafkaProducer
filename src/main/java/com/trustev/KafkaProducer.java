package com.trustev;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class KafkaProducer {
    private String topic;
    private org.apache.kafka.clients.producer.KafkaProducer<byte[], byte[]> producer;
    private boolean isProducerClosed;
    private static PropertyHelper propHelper;
    private List<String> brokers, topics;

    public void init() throws IOException, URISyntaxException {
        setProducerClosed(true);

        if (propHelper ==null) {
            propHelper = new PropertyHelper("kafka.properties");
        }

        topics = propHelper.loadProperty("kafka.topics");
        brokers = propHelper.loadProperty("kafka.brokers");

        topic = topics.get(0);

        Properties props = new Properties();

        props.put("bootstrap.servers", propHelper.reconnectString(brokers));
        props.put("client.id", topic);
        props.put("acks", "all");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        producer = new org.apache.kafka.clients.producer.KafkaProducer<>(props);

        setProducerClosed(false);
    }

    public void makeNewProducer(String brokers){
        if(producer != null){
            producer.close();
        }

        Properties props = new Properties();

        props.put("bootstrap.servers", brokers);
        props.put("client.id", topics.get(0));
        props.put("acks", "all");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        producer = new org.apache.kafka.clients.producer.KafkaProducer<>(props);

        setProducerClosed(false);
    }

    public boolean produceSync(String topic, String value) {
        boolean success = false;

        try {
            ProducerRecord<byte[], byte[]> record = new ProducerRecord<>(topic, value.getBytes());
            Future<RecordMetadata> send = producer.send(record);
            RecordMetadata recordMetadata = send.get();

            success = send.isDone();
            if(recordMetadata!=null) {
                System.out.println(recordMetadata);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally{

        }

        if (success){
            return true;
        } else {
            return false;
        }

    }

    public Map<MetricName, ? extends Metric> dispose() {
        Map<MetricName, ? extends Metric> metrics = producer.metrics();

        producer.close();
        setProducerClosed(true);

        if (metrics != null && !metrics.isEmpty()){

            final StringBuilder strBuilder = new StringBuilder();
            metrics.forEach((metricName, o) -> {
                strBuilder.append(metricName.description() + " - ");
                strBuilder.append(o.value() + "\n");
            });

            String message;
            if (strBuilder !=null){
                message = strBuilder.toString();
            } else {
                message = "No metrics at this time";
            }

            System.out.println(message);

            return metrics;
        } else {
            return null;
        }
    }

    public boolean isProducerClosed() {
        return isProducerClosed;
    }

    private void setProducerClosed(boolean producerClosed) {
        isProducerClosed = producerClosed;
    }
}
