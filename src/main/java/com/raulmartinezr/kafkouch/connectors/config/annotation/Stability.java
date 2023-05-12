package com.raulmartinezr.kafkouch.connectors.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation describes the stability guarantees of the annotated interface, class or method.
 *
 * @since 1.0.0
 */
public @interface Stability {

  /**
   * Types/Methods/Interfaces marked as volatile can change any time and for any reason.
   *
   * <p>
   * They may be volatile for reasons including:
   * </p>
   *
   * <ul>
   * <li>Depends on specific implementation detail within the library which may change in the
   * response.</li>
   * <li>Depends on specific implementation detail within the server which may change in the
   * response.</li>
   * <li>Has been introduced as part of a trial phase for the specific feature.</li>
   * </ul>
   */
  @Documented
  @Retention(RetentionPolicy.RUNTIME)
  @interface Volatile {
  }

  /**
   * No commitment is made about the interface.
   *
   * <p>
   * It may be changed in incompatible ways and dropped from one release to another. The difference
   * between an uncommitted interface and a volatile interface is its maturity and likelihood of
   * being changed. Uncommitted interfaces may mature into committed interfaces.
   * </p>
   */
  @Documented
  @Retention(RetentionPolicy.RUNTIME)
  @interface Uncommitted {
  }

  /**
   * A committed interface is the highest grade of stability, and is the preferred attribute level
   * for consumers of the library.
   *
   * <p>
   * This plugin tries at best effort to preserve committed interfaces between major versions, and
   * changes to committed interfaces within a major version is highly exceptional. Such exceptions
   * may include situations where the interface may lead to data corruption, security holes etc.
   * </p>
   *
   * <p>
   * Explicitly note that backwards-compatible extensions are always allowed since they don't break
   * old code.
   * </p>
   *
   * <p>
   * This is the default interface level for an API, unless the API is specifically marked
   * otherwise.
   * </p>
   */
  @Documented
  @Retention(RetentionPolicy.RUNTIME)
  @interface Committed {
  }

  /**
   * This is internal API and may not be relied on at all.
   *
   * <p>
   * Similar to volatile interfaces, no promises are made about these kinds of interfaces and they
   * change or may vanish at every point in time. But in addition to that, those interfaces are
   * explicitly marked as being designed for internal use and not for external consumption in the
   * first place.
   * </p>
   *
   * <p>
   * Use at your own risk!
   * </p>
   */
  @Documented
  @Retention(RetentionPolicy.RUNTIME)
  @interface Internal {
  }

}
