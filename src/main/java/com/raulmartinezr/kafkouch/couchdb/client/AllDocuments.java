package com.raulmartinezr.kafkouch.couchdb.client;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raulmartinezr.kafkouch.couchdb.client.pojo.Document;
import com.raulmartinezr.kafkouch.couchdb.client.pojo.DocumentOk;
import com.raulmartinezr.kafkouch.couchdb.client.pojo.DatabaseOk;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public abstract class AllDocuments extends BaseClient {

  private static final Logger log = LoggerFactory.getLogger(AllDocuments.class);

  public abstract OkHttpClient getHttpClient();

  public abstract String getUrl();

  private String getDocumentUrl(String database, String documentId) {
    return getUrl() + "/" + database + "/" + documentId;
  }

  public boolean documentHead(String database, String documentId) {
    Request request =
        new Request.Builder().head().url(getDocumentUrl(database, documentId)).build();
    try {
      Response response = getHttpClient().newCall(request).execute();
      if (response.isSuccessful()) {
        return true;
      } else {
        return false;
      }
    } catch (IOException e) {
      log.error("Error checking existence of couchdb document: database={}, documentId={}",
          database, documentId, e);
      return false;
    }
  }

  public Document documentGet(String database, String documentId) {
    Request request = new Request.Builder().url(getDocumentUrl(database, documentId)).get()
        .addHeader("Content-Type", JSON.toString()).addHeader("Accept", acceptHeader).build();
    try (Response response = getHttpClient().newCall(request).execute()) {
      return parseResponse(response, Document.class);
    } catch (IOException e) {
      log.error("Error reading couchdb document: database={}, documentId={}", database, documentId,
          e);
      return null;
    }
  }

  public DocumentOk documentPut(String database, String documentId, String documentJson) {
    Request request = new Request.Builder().url(getDocumentUrl(database, documentId))
        .put(RequestBody.create(documentJson, JSON)).addHeader("Content-Type", JSON.toString())
        .addHeader("Accept", acceptHeader).build();
    try (Response response = getHttpClient().newCall(request).execute()) {
      return parseResponse(response, DocumentOk.class);
    } catch (IOException e) {
      log.error("Error creating couchdb document: database={}, documentId={}", database, documentId,
          e);
      return null;
    }
  }

  public DocumentOk documentDelete(String database, String documentId) throws IOException {
    Request request = new Request.Builder().url(getDocumentUrl(database, documentId)).delete()
        .addHeader("Content-Type", JSON.toString()).addHeader("Accept", acceptHeader).build();
    try (Response response = getHttpClient().newCall(request).execute()) {
      return parseResponse(response, DocumentOk.class);
    } catch (IOException e) {
      log.error("Error deleting couchdb document: database={}, documentId={}", database, documentId,
          e);
      return null;
    }
  }

}
