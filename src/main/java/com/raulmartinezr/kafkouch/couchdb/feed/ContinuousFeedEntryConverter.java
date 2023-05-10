package com.raulmartinezr.kafkouch.couchdb.feed;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ContinuousFeedEntryConverter {
  private ObjectMapper objectMapper;

  public ContinuousFeedEntryConverter() {
    this.objectMapper = new ObjectMapper();
  }

  public ContinuousFeedEntry convertToJavaObject(String feedEntryLine) {
    try {
      return objectMapper.readValue(feedEntryLine, ContinuousFeedEntry.class);
    } catch (IOException e) {
      // Handle JSON parsing exception
      e.printStackTrace();
      return null;
    }
  }

  // public static void main(String[] args) {
  // String feedEntryLine = "{\"db_name\": \"mydb\", \"type\": \"create\",
  // \"seq\": 12345}";

  // ContinuousFeedEntryConverter converter = new ContinuousFeedEntryConverter();
  // ContinuousFeedEntry feedEntry = converter.convertToJavaObject(feedEntryLine);

  // // Process the converted feed entry object
  // if (feedEntry != null) {
  // System.out.println("Parsed Feed Entry:");
  // System.out.println("DB Name: " + feedEntry.getDbName());
  // System.out.println("Type: " + feedEntry.getType());
  // System.out.println("Sequence: " + feedEntry.getSeq());
  // }
  // }
}
