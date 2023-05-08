package com.raulmartinezr.kafkouch.couchdb;

enum CouchdbAuthMethod {
  BASIC, COOKIE
}


public class CouchdbClient {

  private boolean authenticated = false;
  private String url;
  private String username;
  private String password;
  private CouchdbAuthMethod authMethod;

  /**
   * Instantiates a new CouchdbClient.
   */

  public CouchdbClient(CouchdbClientBuilder builder) {
    this.url = builder.getUrl();
    this.username = builder.getUsername();
    this.password = builder.getPassword();
    this.authMethod = builder.getAuthMethod();
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

  private void authenticateBasic() {}

  private void authenticateCookie() {}

}
