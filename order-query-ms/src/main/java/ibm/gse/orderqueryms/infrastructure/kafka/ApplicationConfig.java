package ibm.gse.orderqueryms.infrastructure.kafka;

import java.time.Duration;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 *
 */
public class ApplicationConfig {

    public static final String ORDER_TOPIC = "orders";
    public static final String CONTAINER_TOPIC = "containers";
    public static final String ERROR_TOPIC = "errors";
    public static final Duration CONSUMER_POLL_TIMEOUT = Duration.ofSeconds(10);
    public static final Duration CONSUMER_CLOSE_TIMEOUT = Duration.ofSeconds(10);
    public static final long PRODUCER_TIMEOUT_SECS = 10;
    public static final long PRODUCER_CLOSE_TIMEOUT_SEC = 10;
    public static final long TERMINATION_TIMEOUT_SEC = 10;


    public static Properties getOrderConsumerProperties(String groupid) {
        Properties properties = buildCommonProperties();
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,  groupid);

        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,"true");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,"1000");
//        properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");

        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "order-query");
        return properties;
    }

    public static Properties getOrderConsumerReloadProperties(String groupid) {
        Properties properties = buildCommonProperties();
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,  groupid);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "order-query-reload");
        return properties;
    }

    public static Properties getContainerConsumerProperties(String groupid) {
        Properties properties = buildCommonProperties();
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupid);

        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,"true");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,"1000");
//        properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");

        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "container-query");
        return properties;
    }

    public static Properties getContainerConsumerReloadProperties(String groupid) {
        Properties properties = buildCommonProperties();
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupid);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "container-query-reload");
        return properties;
    }
    /**
     * Take into account the environment variables if set
     *
     * @return common kafka properties
     */
    private static Properties buildCommonProperties() {
        Properties properties = new Properties();
        Map<String, String> env = System.getenv();

        if (env.get("KAFKA_BROKERS") == null) {
            throw new IllegalStateException("Missing environment variable KAFKA_BROKERS");
        }
        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, env.get("KAFKA_BROKERS"));

    		if (env.get("KAFKA_APIKEY") != null) {
          properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
          properties.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
          properties.put(SaslConfigs.SASL_JAAS_CONFIG,
                    "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"token\" password=\""
                            + env.get("KAFKA_APIKEY") + "\";");
          properties.put(SslConfigs.SSL_PROTOCOL_CONFIG, "TLSv1.2");
          properties.put(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, "TLSv1.2");
          properties.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "HTTPS");

          if ("true".equals(env.get("TRUSTSTORE_ENABLED"))){
            properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, env.get("TRUSTSTORE_PATH"));
            properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, env.get("TRUSTSTORE_PWD"));
          }
        }

        return properties;
    }

    public static Properties getProducerProperties(String clientId) {
        Properties properties = buildCommonProperties();
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        return properties;
    }



}
