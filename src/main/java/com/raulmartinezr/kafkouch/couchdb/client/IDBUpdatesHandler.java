package com.raulmartinezr.kafkouch.couchdb.client;

import java.io.BufferedReader;
import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public interface IDBUpdatesHandler {
  public void onResponseSuccessfull(Request request, Response response);

  public void onRead(BufferedReader reader) throws IOException;

  public void onResponseError(Request request, Response response);

  public void onClose(Request request, Response response, OkHttpClient client);

  public void onException(Request request);
}
