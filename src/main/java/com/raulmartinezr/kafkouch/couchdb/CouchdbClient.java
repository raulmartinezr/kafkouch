package com.raulmartinezr.kafkouch.couchdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class CouchdbClient {

  private static final Logger log = LoggerFactory.getLogger(CouchdbClient.class);

  private boolean authenticated = false;
  private String url;
  private String username;
  private String password;
  private CouchdbAuthMethod authMethod;
  private OkHttpClient httpClient;
  private String credentials;

  /**
   * Instantiates a new CouchdbClient.
   */

  public CouchdbClient(CouchdbClientBuilder builder) {
    this.url = builder.getUrl();
    this.username = builder.getUsername();
    this.password = builder.getPassword();
    this.authMethod = builder.getAuthMethod();
    okhttp3.OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
    if (this.authMethod == CouchdbAuthMethod.COOKIE) {
      httpClientBuilder.addInterceptor(
          new AuthenticatorInterceptor(this.httpClient, this.url, this.username, this.password));
    }
    this.httpClient = httpClientBuilder.build();
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

  // private Response doRequest(Request request) {
  // Builder newBuilder = request.newBuilder();
  // if (this.authMethod == CouchdbAuthMethod.BASIC) {
  // newBuilder.addHeader("Authorization", this.credentials);
  // }
  // // Cookie auth already has the client authenticated and session cookie stored
  // in
  // // client object

  // return null;
  // }

  public static class CouchdbClientBuilder {

    private boolean connect = false;
    private String url;
    private String username;
    private String password;
    private CouchdbAuthMethod authMethod;

    public CouchdbClientBuilder() {}

    protected void validate() {
      /**
       * Validates all required inputs are defined and not empty.
       */
      assert this.url != null && this.url.isEmpty() : "url must not be empty";
      assert this.username != null && this.username.isEmpty() : "username must not be empty";
      assert this.password != null && this.password.isEmpty() : "username must not be empty";
      assert this.authMethod != null && (this.authMethod instanceof CouchdbAuthMethod)
          : "authMethod must be an instance of CouchdbAuthMethod";
    }

    public CouchdbClient build() {
      /**
       * Builds a CouchdbClient instance.
       */
      this.validate();
      CouchdbClient client = new CouchdbClient(this);
      if (this.connect) {
        client.authenticate();
      }
      return client;
    }

    /**
     * @param connect the connect to set
     */
    public CouchdbClientBuilder setConnect(boolean connect) {
      this.connect = connect;
      return this;
    }

    /**
     * @param url the url to set
     */
    public CouchdbClientBuilder setUrl(String url) {
      this.url = url;
      return this;
    }

    /**
     * @param username the username to set
     * @return
     */
    public CouchdbClientBuilder setUsername(String username) {
      this.username = username;
      return this;
    }

    /**
     * @param password the password to set
     */
    public CouchdbClientBuilder setPassword(String password) {
      this.password = password;
      return this;
    }

    /**
     * @param authMethod the authMethod to set
     */
    public CouchdbClientBuilder setAuthMethod(CouchdbAuthMethod authMethod) {
      this.authMethod = authMethod;
      return this;
    }

    /**
     * @return the connect
     */
    public boolean isConnect() {
      return connect;
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

  }

  public enum CouchdbAuthMethod {
    BASIC, COOKIE
  }
}
