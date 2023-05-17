package com.raulmartinezr.kafkouch.util;

import java.util.HashSet;
import java.util.Set;

public class ThreadSafeSetHandler<T> {
  private Set<T> set;

  public ThreadSafeSetHandler() {
    set = new HashSet<T>();
  }

  public ThreadSafeSetHandler(Set<T> set) {
    this.set = set;
  }

  public synchronized <E extends T> void add(E item) {
    this.set.add(item);
  }

  public synchronized void remove(T item) {
    this.set.remove(item);
  }

  public synchronized boolean contains(T item) {
    return this.set.contains(item);
  }

  public synchronized int size() {
    return this.set.size();
  }

  public synchronized Set<T> getSetSnapshot() {
    return new HashSet<T>(this.set);
  }

  public synchronized void setSet(Set<T> set) {
    this.set = set;
  }
}
