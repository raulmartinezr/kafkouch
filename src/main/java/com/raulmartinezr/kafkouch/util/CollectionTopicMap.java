package com.raulmartinezr.kafkouch.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionTopicMap {
  private CollectionTopicMap() {
    throw new AssertionError("not instantiable");
  }

  public static Map<String, String> parseCollectionToTopic(List<String> toTopic) {
    Map<String, String> result = new HashMap<>();
    for (String entry : toTopic) {
      String[] components = entry.split("=", -1);
      if (components.length != 2) {
        throw new IllegalArgumentException("Bad entry: '" + entry
            + "'. Expected exactly one equals (=) character separating topic and collection.");
      }
      result.put(components[0], components[1]);
    }
    return result;
  }

}
