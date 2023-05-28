package com.raulmartinezr.kafkouch.couchdb.client.pojo;

public class DocumentOk {
  private String id;
  private boolean ok;
  private String rev;

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return the ok
   */
  public boolean isOk() {
    return ok;
  }

  /**
   * @param ok the ok to set
   */
  public void setOk(boolean ok) {
    this.ok = ok;
  }

  /**
   * @return the rev
   */
  public String getRev() {
    return rev;
  }

  /**
   * @param rev the rev to set
   */
  public void setRev(String rev) {
    this.rev = rev;
  }

}
