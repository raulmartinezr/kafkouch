package com.raulmartinezr.kafkouch.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class GenericClass<T> {

  public Class<?> getGenericTypeClass() {
    Type genericSuperclass = getClass().getGenericSuperclass();
    if (genericSuperclass instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
      Type[] typeArguments = parameterizedType.getActualTypeArguments();
      if (typeArguments.length > 0) {
        Type typeArgument = typeArguments[0];
        if (typeArgument instanceof Class) {
          return (Class<?>) typeArgument;
        }
      }
    }
    return null;
  }
}
