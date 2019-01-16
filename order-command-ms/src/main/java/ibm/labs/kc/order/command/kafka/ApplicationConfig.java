package ibm.labs.kc.order.command.kafka;

import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * This class is to read configuration from properties file and keep in a
 * properties object. It also provides a set of method to define kafka config
 * parameters
 * 
 * @author jerome boyer
 *
 */
public class ApplicationConfig {

    public static final String ORDER_TOPIC = "orders";
    public static final long PRODUCER_TIMEOUT_SECS = 10;

    public static Properties getProducerProperties() {
        Properties properties = buildCommonProperties();
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "order-producer");
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
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

        if ("IBMCLOUD".equals(env.get("KAFKA_ENV")) || "ICP".equals(env.get("KAFKA_ENV"))) {
            if (env.get("KAFKA_BROKERS") == null) {
                throw new IllegalStateException("Missing environment variable KAFKA_BROKERS");
            }
            if (env.get("KAFKA_APIKEY") == null) {
                throw new IllegalStateException("Missing environment variable KAFKA_APIKEY");
            }
            properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, env.get("KAFKA_BROKERS"));
            properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
            properties.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
            properties.put(SaslConfigs.SASL_JAAS_CONFIG,
                    "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"token\" password=\""
                            + env.get("KAFKA_APIKEY") + "\";");
            properties.put(SslConfigs.SSL_PROTOCOL_CONFIG, "TLSv1.2");
            properties.put(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, "TLSv1.2");
            properties.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "HTTPS");
        } else {
            if (env.get("KAFKA_BROKERS") == null) {
                properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            } else {
                properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, env.get("KAFKA_BROKERS"));
            }
        }

        return properties;
    }

}
