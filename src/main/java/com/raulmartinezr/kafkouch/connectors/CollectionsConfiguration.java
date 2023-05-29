package com.raulmartinezr.kafkouch.connectors;

import java.util.Map;

import com.raulmartinezr.kafkouch.util.CollectionFilterType;
import com.raulmartinezr.kafkouch.util.EnabledCollectionsType;

public class CollectionsConfiguration {

  Map<String, EnabledCollectionsType> databaseToCollections;
  Map<String, String> collectionToTopic;

  /**
   * @param databaseToCollections
   * @param collectionToTopic
   * @param collectionToFilter
   */
  public CollectionsConfiguration(Map<String, EnabledCollectionsType> databaseToCollections,
      Map<String, String> collectionToTopic, Map<String, CollectionFilterType> collectionToFilter) {
    this.databaseToCollections = databaseToCollections;
    this.collectionToTopic = collectionToTopic;
    this.collectionToFilter = collectionToFilter;
  }

  Map<String, CollectionFilterType> collectionToFilter;

  /**
   * @return the databaseToCollections
   */
  public Map<String, EnabledCollectionsType> getDatabaseToCollections() {
    return databaseToCollections;
  }

  /**
   * @param databaseToCollections the databaseToCollections to set
   */
  public void setDatabaseToCollections(Map<String, EnabledCollectionsType> databaseToCollections) {
    this.databaseToCollections = databaseToCollections;
  }

  /**
   * @return the collectionToTopic
   */
  public Map<String, String> getCollectionToTopic() {
    return collectionToTopic;
  }

  /**
   * @param collectionToTopic the collectionToTopic to set
   */
  public void setCollectionToTopic(Map<String, String> collectionToTopic) {
    this.collectionToTopic = collectionToTopic;
  }

  /**
   * @return the collectionToFilter
   */
  public Map<String, CollectionFilterType> getCollectionToFilter() {
    return collectionToFilter;
  }

  /**
   * @param collectionToFilter the collectionToFilter to set
   */
  public void setCollectionToFilter(Map<String, CollectionFilterType> collectionToFilter) {
    this.collectionToFilter = collectionToFilter;
  }

}
