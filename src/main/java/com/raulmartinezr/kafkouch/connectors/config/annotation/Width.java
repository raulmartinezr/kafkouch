package com.raulmartinezr.kafkouch.connectors.config.annotation;

import org.apache.kafka.common.config.ConfigDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Width {
    ConfigDef.Width value();
}