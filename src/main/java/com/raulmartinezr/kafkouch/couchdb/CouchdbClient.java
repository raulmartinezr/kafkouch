package com.raulmartinezr.kafkouch.couchdb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raulmartinezr.kafkouch.couchdb.client.IDBUpdatesHandler;
import com.raulmartinezr.kafkouch.couchdb.client.pojo.DatabaseOk;
import com.raulmartinezr.kafkouch.couchdb.client.pojo.Document;
import com.raulmartinezr.kafkouch.couchdb.client.pojo.DocumentOk;

public class CouchdbClient {

  private static final Logger log = LoggerFactory.getLogger(CouchdbClient.class);

  ObjectMapper objectMapper = new ObjectMapper();
  public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
  public static final String acceptHeader = "application/json";

  private static final int List = 0;

  private boolean authenticated = false;
  private String url;
  private String username;
  private String password;
  private CouchdbAuthMethod authMethod;
  private OkHttpClient httpClient;
  private CookieJar cookieJar;
  private String credentials;
  private long readTimeout = Integer.MAX_VALUE;

  /**
   * Instantiates a new CouchdbClient.
   */

  public CouchdbClient(CouchdbClientBuilder builder) {
    this.url = builder.getUrl();
    this.username = builder.getUsername();
    this.password = builder.getPassword();
    this.authMethod = builder.getAuthMethod();
    this.readTimeout = builder.getReadTimeout();
    okhttp3.OkHttpClient.Builder httpClientBuilder =
        new OkHttpClient.Builder().readTimeout(this.readTimeout, TimeUnit.MILLISECONDS);
    this.cookieJar = this.buildCookieJar();
    this.httpClient = httpClientBuilder.cookieJar(cookieJar).build();

    if (this.authMethod == CouchdbAuthMethod.COOKIE) {
      httpClientBuilder.addInterceptor(
          new AuthenticatorInterceptor(this.httpClient, this.url, this.username, this.password));
    }

  }

  private CookieJar buildCookieJar() {
    CookieJar cookieJar = new CookieJar() {
      private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

      @Override
      public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        cookieStore.put(url.host(), cookies);
      }

      @Override
      public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies = cookieStore.get(url.host());
        return cookies != null ? cookies : new ArrayList<>();
      }

    };
    return cookieJar;
  }

  /**
   * @return the connect
   */
  public boolean isAuthenticated() {
    return authenticated;
  }

  /**
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * @return the username
   */
  public String getUsername() {
    return username;
  }

  /**
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * @return the authMethod
   */
  public CouchdbAuthMethod getAuthMethod() {
    return authMethod;
  }

  /**
   * @return the httpClient
   */
  public OkHttpClient getHttpClient() {
    return httpClient;
  }

  /**
   * @return the cookieJar
   */
  public CookieJar getCookieJar() {
    return cookieJar;
  }

  /**
   * @return the readTimeout
   */
  public long getReadTimeout() {
    return readTimeout;
  }

  public void authenticate() {
    switch (this.authMethod) {
      case BASIC:
        this.authenticateBasic();
        break;
      case COOKIE:
        this.authenticateCookie();
        break;
      default:
        log.warn("Authentication method {} not supported", this.authMethod);
        break;
    }
  }

  private void authenticateBasic() {
    /**
     * In case of basic authentication, we need the credentials.
     */
    this.credentials = Credentials.basic(this.username, this.password);
  }

  private void authenticateCookie() {
    String requestUrl = this.url + "/_session";
    RequestBody requestBody =
        new FormBody.Builder().add("name", this.username).add("password", this.password).build();
    Request request = new Request.Builder().url(requestUrl).post(requestBody).build();
    try (Response response = this.httpClient.newCall(request).execute()) {
      // Handle response
      if (response.isSuccessful()) {
        // Authentication successful
        String responseBody = response.body().string();
        System.out.println("Authentication successful. Response: " + responseBody);
      } else {
        // Authentication failed
        System.out.println("Authentication failed: " + response.code());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * Database operations
   */

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
      return parseResponse(response, new TypeReference<DatabaseOk>() {});
    } catch (IOException e) {
      log.error("Error creating couchdb database", e);
      return null;
    }
  }

  public DatabaseOk databaseDelete(String database) throws IOException {
    Request request = new Request.Builder().url(getUrl() + "/" + database).delete()
        .addHeader("Content-Type", JSON.toString()).addHeader("Accept", acceptHeader).build();
    try (Response response = getHttpClient().newCall(request).execute()) {
      return parseResponse(response, new TypeReference<DatabaseOk>() {});
    } catch (IOException e) {
      log.error("Error deleting couchdb database", e);
      return null;
    }
  }

  /**
   * Document operations
   */
  private String documentGetUrl(String database, String documentId) {
    return getUrl() + "/" + database + "/" + documentId;
  }

  public boolean documentHead(String database, String documentId) {
    Request request =
        new Request.Builder().head().url(documentGetUrl(database, documentId)).build();
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
    Request request = new Request.Builder().url(documentGetUrl(database, documentId)).get()
        .addHeader("Content-Type", JSON.toString()).addHeader("Accept", acceptHeader).build();
    try (Response response = getHttpClient().newCall(request).execute()) {
      return parseResponse(response, new TypeReference<Document>() {});
    } catch (IOException e) {
      log.error("Error reading couchdb document: database={}, documentId={}", database, documentId,
          e);
      return null;
    }
  }

  public DocumentOk documentPut(String database, String documentId, String documentJson) {
    Request request = new Request.Builder().url(documentGetUrl(database, documentId))
        .put(RequestBody.create(documentJson, JSON)).addHeader("Content-Type", JSON.toString())
        .addHeader("Accept", acceptHeader).build();
    try (Response response = getHttpClient().newCall(request).execute()) {
      return parseResponse(response, new TypeReference<DocumentOk>() {});
    } catch (IOException e) {
      log.error("Error creating couchdb document: database={}, documentId={}", database, documentId,
          e);
      return null;
    }
  }

  public DocumentOk documentDelete(String database, String documentId) throws IOException {
    Request request = new Request.Builder().url(documentGetUrl(database, documentId)).delete()
        .addHeader("Content-Type", JSON.toString()).addHeader("Accept", acceptHeader).build();
    try (Response response = getHttpClient().newCall(request).execute()) {
      return parseResponse(response, new TypeReference<DocumentOk>() {});
    } catch (IOException e) {
      log.error("Error deleting couchdb document: database={}, documentId={}", database, documentId,
          e);
      return null;
    }
  }

  /*
   * Server operations
   */

  public Set<String> serverAllDatabasesGet() {
    String url = getUrl() + "/_all_dbs";
    Request request = new Request.Builder().url(url).get()
        .addHeader("Content-Type", JSON.toString()).addHeader("Accept", acceptHeader).build();
    try (Response response = getHttpClient().newCall(request).execute()) {
      return parseResponse(response, new TypeReference<Set<String>>() {});
    } catch (IOException e) {
      log.error("Error reading all couchdb databases", e);
      return null;
    }
  }

  public void serverDBUpdatesGet(FeedType feed, String since, long heartbeat, long timeout,
      IDBUpdatesHandler updatesHandler) {
    String globalChangesFeedUrl = getUrl() + "/_db_updates?feed=" + feed.value + "&heartbeat="
        + heartbeat + "&timeout=" + timeout + "&since=" + since;
    List<Cookie> cookies = getCookieJar().loadForRequest(HttpUrl.parse(getUrl()));
    Headers.Builder headerBuilder = new Headers.Builder();

    for (Cookie cookie : cookies) {
      headerBuilder.add("Cookie", cookie.name() + "=" + cookie.value());
    }
    Request request =
        new Request.Builder().url(globalChangesFeedUrl).headers(headerBuilder.build()).build();
    BufferedReader reader = null;
    InputStream inputStream = null;
    Response response = null;
    try {
      response = getHttpClient().newCall(request).execute();
      if (response.isSuccessful()) {
        updatesHandler.onResponseSuccessfull(request, response);
        inputStream = response.body().byteStream();
        reader = new BufferedReader(new InputStreamReader(inputStream));
        updatesHandler.onRead(reader);

      } else {
        updatesHandler.onResponseError(request, response);
      }

    } catch (IOException e) {
      log.error(
          "Exception reading couchdb global changes: feed={}, heartbeat={}, timeout={},since= {}",
          feed.value, heartbeat, timeout, since, e);
      updatesHandler.onException(request);
    } finally {
      try {
        if (reader != null)
          reader.close();
        if (inputStream != null)
          inputStream.close();
      } catch (IOException closeE) {
        log.error("Error closing resources", closeE);
      }
      updatesHandler.onClose(request, response, getHttpClient());
    }

  }

  /*
   * Response parsing
   */
  private <T> T parseResponse(Response response, TypeReference<T> typeReference)
      throws IOException {
    if (response.isSuccessful()) {
      return this.objectMapper.readValue(response.body().string(), typeReference);
    } else {
      throw new IOException("Request failed with status code: " + response.code());
    }
  }

  /**
   * Enums
   */
  public enum CouchdbAuthMethod {
    BASIC, COOKIE
  }

  public enum FeedType {
    NORMAL("normal"), LONGPOOLL("longpoll"), CONTINUOUS("continuous"), EVENTSOURCE("eventsource");

    private final String value;

    FeedType(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }
  }
}
