package com.raulmartinezr.kafkouch.util;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.type.TypeReference;

public class JsonMapper {

  private static final Logger log = LoggerFactory.getLogger(JsonMapper.class);

  private JsonMapper() {
    throw new AssertionError("not instantiable");
  }

  // ObjectMapper is mutable. To prevent it from being accidentally (or
  // maliciously) reconfigured,
  // don't expose the ObjectMapper outside this class.
  private static final ObjectMapper mapper = newObjectMapper();

  public static ObjectMapper newObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.getFactory().setStreamReadConstraints(
        StreamReadConstraints.defaults().rebuild().maxStringLength(20 * 1024 * 1024) // increase
                                                                                     // from 5 MB to
                                                                                     // 20 MiB
            .build());
    return mapper;
  }

  // Instead, expose immutable reader and writer for advanced use cases.
  private static final ObjectReader reader = mapper.reader();
  private static final ObjectWriter writer = mapper.writer();

  /**
   * Encodes the given input into a byte array, formatted non-pretty.
   *
   * @param input the java object as input
   * @return the json encoded byte array.
   * @throws JsonProcessingException
   */
  public static byte[] encodeAsBytes(final Object input) {
    try {
      return mapper.writeValueAsBytes(input);
    } catch (JsonProcessingException ex) {
      log.error("Could not encode into JSON: " + input.toString(), ex);
      return new byte[0];
    }

  }

  /**
   * Encodes the given input into a byte array, formatted pretty.
   *
   * @param input the java object as input
   * @return the json encoded byte array.
   * @throws Exception
   */
  public static byte[] encodeAsBytesPretty(final Object input) throws Exception {
    try {
      return mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(input);
    } catch (JsonProcessingException ex) {
      log.error("Could not encode into JSON: " + input.toString(), ex);
      return new byte[0];
    }
  }

  /**
   * } catch (Exception ex) { throw new JsonProcessingException("Could not decode from JSON: " +
   * redactUser(input), ex); } Encodes the given input into a String, formatted non-pretty.
   *
   * @param input the java object as input
   * @return the json encoded String.
   */
  public static String encodeAsString(final Object input) {
    try {
      return mapper.writeValueAsString(input);
    } catch (JsonProcessingException ex) {
      log.error("Could not encode into JSON: " + input.toString(), ex);
      return "";
    }
  }

  /**
   * Encodes the given input into a String, formatted pretty.
   *
   * @param input the java object as input
   * @return the json encoded String.
   */
  public static String encodeAsStringPretty(final Object input) {
    try {
      return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(input);
    } catch (JsonProcessingException ex) {
      log.error("Could not encode into JSON: " + input.toString(), ex);
      return "";
    }
  }

  /**
   * Decodes a byte array into the given class.
   *
   * @param input the input byte array.
   * @param clazz the clazz which should be decoded into.
   * @param <T> generic type used for inference.
   * @return the created instance.
   */
  public static <T> T decodeInto(byte[] input, Class<T> clazz) {
    try {
      return mapper.readValue(input, clazz);
    } catch (IOException ex) {
      log.error("Could not decode from JSON: " + input.toString(), ex);
      return null;
    }
  }

  /**
   * Decodes a String into the given class.
   *
   * @param input the input byte array.
   * @param clazz the clazz which should be decoded into.
   * @param <T> generic type used for inference.
   * @return the created instance.
   */
  public static <T> T decodeInto(String input, Class<T> clazz) {
    try {
      return mapper.readValue(input, clazz);
    } catch (IOException ex) {
      log.error("Could not decode from JSON: " + input.toString(), ex);
      return null;
    }
  }

  /**
   * Decodes a byte array into the given type.
   *
   * @param input the input byte array.
   * @param type the type which should be decoded into.
   * @param <T> generic type used for inference.
   * @return the created instance.
   */

  public static <T> T decodeInto(byte[] input, TypeReference<T> type) {
    try {
      return mapper.readValue(input, type);
    } catch (IOException ex) {
      log.error("Could not decode from JSON: " + input.toString(), ex);
      return null;
    }
  }

  /**
   * Decodes a String into the given type.
   *
   * @param input the input byte array.
   * @param type the type which should be decoded into.
   * @param <T> generic type used for inference.
   * @return the created instance.
   */

  public static <T> T decodeInto(String input, TypeReference<T> type) {
    try {
      return mapper.readValue(input, type);
    } catch (IOException ex) {
      log.error("Could not decode from JSON: " + input.toString(), ex);
      return null;
    }
  }

  /**
   * Decodes a byte array into a tree of JSON nodes.
   *
   * @param input the input byte array.
   * @return the created node.
   */
  public static JsonNode decodeIntoTree(byte[] input) {
    try {
      return mapper.readTree(input);
    } catch (IOException ex) {
      log.error("Could not decode from JSON: " + input.toString(), ex);
      return null;
    }
  }

  /**
   * Decodes a string into a tree of JSON nodes.
   *
   * @param input the input byte array.
   * @return the created node.
   */
  public static JsonNode decodeIntoTree(String input) {
    try {
      return mapper.readTree(input);
    } catch (IOException ex) {
      log.error("Could not decode from JSON: " + input.toString(), ex);
      return null;
    }
  }

  /**
   * Converts an object to the requested type using
   * {@link ObjectMapper#convertValue(Object, Class)}.
   */
  public static <T> T convertValue(Object fromValue, Class<T> toValueType) {
    return mapper.convertValue(fromValue, toValueType);
  }

  /**
   * Converts an object to the requested type using
   * {@link ObjectMapper#convertValue(Object, TypeReference)}.
   */
  public static <T> T convertValue(Object fromValue, TypeReference<T> toValueTypeRef) {
    return mapper.convertValue(fromValue, toValueTypeRef);
  }

  /**
   * Returns an ObjectReader for advanced use cases.
   */
  public static ObjectReader reader() {
    return reader;
  }

  /**
   * Returns an ObjectWriter for advanced use cases.
   */
  public static ObjectWriter writer() {
    return writer;
  }

  /**
   * Returns a new empty ObjectNode.
   */
  public static ObjectNode createObjectNode() {
    return mapper.createObjectNode();
  }

  /**
   * Returns a new empty ArrayNode.
   */
  public static ArrayNode createArrayNode() {
    return mapper.createArrayNode();
  }
}
