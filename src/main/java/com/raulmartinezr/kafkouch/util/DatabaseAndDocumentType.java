package com.raulmartinezr.kafkouch.util;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class DatabaseAndDocumentType {
  private final String database;
  private final String documentType;

  public static DatabaseAndDocumentType parse(String databaseAndDocumentType) {
    String[] split = databaseAndDocumentType.split("\\.", -1);
    if (split.length != 2) {
      throw new IllegalArgumentException(
          "Expected qualified collection name (database.documentType) but got: "
              + databaseAndDocumentType);
    }
    return new DatabaseAndDocumentType(split[0], split[1]);
  }

  public DatabaseAndDocumentType(String database, String documentType) {
    this.database = requireNonNull(database);
    this.documentType = requireNonNull(documentType);
  }

  public String getDatabase() {
    return database;
  }

  public String getDocumentType() {
    return documentType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    DatabaseAndDocumentType that = (DatabaseAndDocumentType) o;
    return database.equals(that.database) && documentType.equals(that.documentType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(database, documentType);
  }
}
