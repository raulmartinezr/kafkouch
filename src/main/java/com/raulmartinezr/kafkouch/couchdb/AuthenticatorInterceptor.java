package com.raulmartinezr.kafkouch.couchdb;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AuthenticatorInterceptor implements Interceptor {

  private final OkHttpClient client;
  private String url;
  private String username;
  private String password;

  public AuthenticatorInterceptor(OkHttpClient client, String url, String username,
      String password) {
    this.client = client;
    this.url = url;
    this.username = username;
    this.password = password;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    Response response = chain.proceed(request);

    // Check if the response is unauthenticated
    if (response.code() == 401) {
      // Create a new authentication request with the necessary credentials
      Request newAuthRequest = new Request.Builder().url(this.url + "/_session").post(
          new FormBody.Builder().add("name", this.username).add("password", this.password).build())
          .build();

      // Execute the new authentication request
      try (Response newAuthResponse = client.newCall(newAuthRequest).execute()) {
        // Check if the new authentication request is successful
        if (newAuthResponse.isSuccessful()) {
          // Make the original request again with the updated cookies
          Request authenticatedRequest =
              request.newBuilder().header("Cookie", newAuthResponse.header("Set-Cookie")).build();

          return chain.proceed(authenticatedRequest);
        }
      }
    }

    return response;
  }
}
