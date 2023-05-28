package com.raulmartinezr.kafkouch.couchdb.client.pojo;

public class Error {
  private String error;
  private String reason;

  /**
   * @return the error
   */
  public String getError() {
    return error;
  }

  /**
   * @param error the error to set
   */
  public void setError(String error) {
    this.error = error;
  }

  /**
   * @return the reason
   */
  public String getReason() {
    return reason;
  }

  /**
   * @param reason the reason to set
   */
  public void setReason(String reason) {
    this.reason = reason;
  }

}
