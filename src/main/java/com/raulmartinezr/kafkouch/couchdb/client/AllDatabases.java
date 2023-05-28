package com.raulmartinezr.kafkouch.couchdb.client;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raulmartinezr.kafkouch.couchdb.client.pojo.DatabaseOk;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public abstract class AllDatabases extends BaseClient {

  private static final Logger log = LoggerFactory.getLogger(AllDatabases.class);

  public abstract OkHttpClient getHttpClient();

  public abstract String getUrl();

  public boolean databaseHead(String database) {
    Request request = new Request.Builder().head().url(getUrl() + "/" + database).build();
    try {
      Response response = getHttpClient().newCall(request).execute();
      if (response.isSuccessful()) {
        return true;
      } else {
        return false;
      }
    } catch (IOException e) {
      log.error("Unexpected error in couchdb client", e);
      return false;
    }
  }

  public DatabaseOk databasePut(String database) {
    return databasePut(database, 8, 3, false);
  }

  public DatabaseOk databasePut(String database, boolean partitioned) {
    return databasePut(database, 8, 3, partitioned);
  }

  public DatabaseOk databasePut(String database, int q, int n, boolean partitioned) {
    Request request = new Request.Builder()
        .url(getUrl() + "/" + database + "?q=" + q + "&n=" + n + "&partitioned=" + partitioned)
        .put(RequestBody.create("", JSON)).addHeader("Content-Type", JSON.toString())
        .addHeader("Accept", acceptHeader).build();
    try (Response response = getHttpClient().newCall(request).execute()) {
      return parseResponse(response, DatabaseOk.class);
    } catch (IOException e) {
      log.error("Error creating couchdb database", e);
      return null;
    }
  }

  public DatabaseOk databaseDelete(String database) throws IOException {
    Request request = new Request.Builder().url(getUrl() + "/" + database).delete()
        .addHeader("Content-Type", JSON.toString()).addHeader("Accept", acceptHeader).build();
    try (Response response = getHttpClient().newCall(request).execute()) {
      return parseResponse(response, DatabaseOk.class);
    } catch (IOException e) {
      log.error("Error deleting couchdb database", e);
      return null;
    }
  }

}
