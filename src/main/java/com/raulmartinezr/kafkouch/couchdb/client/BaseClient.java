package com.raulmartinezr.kafkouch.couchdb.client;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.MediaType;
import okhttp3.Response;

public class BaseClient {

  ObjectMapper objectMapper = new ObjectMapper();
  public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
  public static final String acceptHeader = "application/json";

  public <T> T parseResponse(Response response, Class<T> pojoClass) throws IOException {
    if (response.isSuccessful()) {
      return this.objectMapper.readValue(response.body().string(), pojoClass);
    } else {
      throw new IOException("Request failed with status code: " + response.code());
    }
  }

}
