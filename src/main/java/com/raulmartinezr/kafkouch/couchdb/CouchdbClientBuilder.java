package com.raulmartinezr.kafkouch.couchdb;

import com.raulmartinezr.kafkouch.couchdb.CouchdbClient.CouchdbAuthMethod;

public class CouchdbClientBuilder {

  private boolean connect = false;
  private String url;
  private String username;
  private String password;
  private CouchdbAuthMethod authMethod;
  private long readTimeout = Long.MAX_VALUE;

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
   * @return the readTimeout
   */
  public long getReadTimeout() {
    return readTimeout;
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

  /**
   * @param readTimeout the readTimeout to set
   */
  public CouchdbClientBuilder setReadTimeout(long readTimeout) {
    this.readTimeout = readTimeout;
    return this;
  }

}


