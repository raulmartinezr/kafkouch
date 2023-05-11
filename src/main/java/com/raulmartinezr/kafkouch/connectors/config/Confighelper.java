package com.raulmartinezr.kafkouch.connectors.config;

import java.util.Map;
import java.util.function.Consumer;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;

public class ConfigHelper {
  private static final KafkaConfigProxyFactory factory =
      new KafkaConfigProxyFactory("couchbase");

  private ConfigHelper() {
    throw new AssertionError("not instantiable");
  }

  public static ConfigDef define(Class<?> configClass) {
    return factory.define(configClass);
  }

  public static <T> T parse(Class<T> configClass, Map<String, String> props) {
    return factory.newProxy(configClass, props);
  }

  public static <T> String keyName(Class<T> configClass, Consumer<T> methodInvoker) {
    return factory.keyName(configClass, methodInvoker);
  }

  public interface SimpleValidator<T> {
    void validate(T value) throws Exception;
  }

  @SuppressWarnings("unchecked")
  public static <T> ConfigDef.Validator validate(SimpleValidator<T> validator, String description) {
    return new ConfigDef.Validator() {
      @Override
      public String toString() {
        return description;
      }

      @Override
      public void ensureValid(String name, Object value) {
        try {
          validator.validate((T) value);
        } catch (Exception e) {
          throw new ConfigException(name, value, e.getMessage());
        }
      }
    };
  }
}