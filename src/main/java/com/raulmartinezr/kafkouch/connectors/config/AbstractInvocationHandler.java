package com.raulmartinezr.kafkouch.connectors.config;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

abstract class AbstractInvocationHandler implements InvocationHandler {
  private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

  private final String toStringPrefix;

  public AbstractInvocationHandler(String toStringPrefix) {
    this.toStringPrefix = toStringPrefix;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] argsMaybeNull) throws Throwable {
    Object[] args = argsMaybeNull == null ? EMPTY_OBJECT_ARRAY : argsMaybeNull;

    if ("equals".equals(method.getName()) && args.length == 1
        && method.getParameterTypes()[0].equals(Object.class)) {
      return proxy == args[0];
    }

    if ("hashCode".equals(method.getName()) && args.length == 0) {
      return System.identityHashCode(proxy);
    }

    if ("toString".equals(method.getName()) && args.length == 0) {
      return toStringPrefix + "@" + Integer.toHexString(proxy.hashCode());
    }

    return doInvoke(proxy, method, args);
  }

  protected abstract Object doInvoke(Object proxy, Method method, Object[] args);
}
