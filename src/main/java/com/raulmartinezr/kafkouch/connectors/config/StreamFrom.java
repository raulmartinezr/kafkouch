package com.raulmartinezr.kafkouch.connectors.config;

/**
 * The resume modes supported by the Kafka connector.
 */
public enum StreamFrom {
  SAVED_OFFSET_OR_BEGINNING, SAVED_OFFSET_OR_NOW, BEGINNING, NOW;

  /**
   * Returns the "fallback" mode for modes that prefer saved offsets, otherwise returns
   * {@code this}.
   */
  public StreamFrom withoutSavedOffset() {
    switch (this) {
      case SAVED_OFFSET_OR_BEGINNING:
      case BEGINNING:
        return BEGINNING;

      case SAVED_OFFSET_OR_NOW:
      case NOW:
        return NOW;

      default:
        throw new AssertionError();
    }
  }

  /**
   * Returns true if this mode prefers to use saved offset.
   */
  public boolean isSavedOffset() {
    return this != withoutSavedOffset();
  }

  // public com.couchbase.client.dcp.StreamFrom asDcpStreamFrom() {
  // switch (this) {
  // case BEGINNING:
  // return com.couchbase.client.dcp.StreamFrom.BEGINNING;
  // case NOW:
  // return com.couchbase.client.dcp.StreamFrom.NOW;
  // default:
  // throw new IllegalStateException(this + " has no DCP counterpart");
  // }
  // }
}
