package com.raulmartinezr.kafkouch.connectors.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the config key's value should be obtained from the named environment variable (if the
 * variable is set).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EnvironmentVariable {
  String value();
}
