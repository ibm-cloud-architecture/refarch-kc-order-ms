package it;

import org.microshed.testing.SharedContainerConfiguration;
import org.microshed.testing.testcontainers.ApplicationContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;

public class ContainerConfig implements SharedContainerConfiguration {
    private static Network network = Network.newNetwork();

    @Container
    public static KafkaContainer kafka = new KafkaContainer()
                    .withNetworkAliases("kafka")
                    .withNetwork(network);
                    
    @Container
    public static ApplicationContainer app = new ApplicationContainer()
            .withNetwork(network)
            .dependsOn(kafka)
            .withEnv("KAFKA_BROKERS", "kafka:9092")
            .withReadinessPath("/health/ready");
}
