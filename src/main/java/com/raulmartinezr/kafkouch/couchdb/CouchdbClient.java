package com.raulmartinezr.kafkouch.couchdb;

import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Request.Builder;

enum CouchdbAuthMethod {
  BASIC, COOKIE
}

public class CouchdbClient {

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

  public void authenticate() {
    switch (this.authMethod) {
      case BASIC -> this.authenticateBasic();
      case COOKIE -> this.authenticateCookie();
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
    RequestBody requestBody = new FormBody.Builder().add("name", this.username).add("password", this.password).build();
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

  private Response doRequest(Request request) {
    Builder newBuilder = request.newBuilder();
    if (this.authMethod == CouchdbAuthMethod.BASIC) {
      newBuilder.addHeader("Authorization", this.credentials);
    }
    // Cookie auth already has the client authenticated and session cookie stored in
    // client object

    return null;
  }
}
