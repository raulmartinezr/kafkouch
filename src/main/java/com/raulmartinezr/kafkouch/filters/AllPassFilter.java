package com.raulmartinezr.kafkouch.filters;

import com.raulmartinezr.kafkouch.couchdb.feed.ContinuousFeedEntry;

public class AllPassFilter implements Filter {

  @Override
  public boolean pass(final ContinuousFeedEntry feedEntry) {
    return true;
  }
}
