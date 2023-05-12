package com.raulmartinezr.kafkouch.connectors.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the display name of the config key, overriding the default inference strategy (which is
 * to capitalize the method name and insert a space between capital letters).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DisplayName {
  String value();
}
