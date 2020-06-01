package it;

import ibm.gse.orderqueryms.infrastructure.kafka.ApplicationConfig;

import java.util.Optional;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;

public class TestApplicationConfig implements Config {

    @Override
    public String getValue(String propertyName, Class propertyType) {
        switch (propertyName) {
            case "order.topic":
            return "orders";
            case "container.topic":
            return "containers";
            default:
            return null;
        }

    }   

    @Override
    public <T> Optional<T> getOptionalValue(String propertyName, Class<T> propertyType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<String> getPropertyNames() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<ConfigSource> getConfigSources() {
        // TODO Auto-generated method stub
        return null;
    }
}