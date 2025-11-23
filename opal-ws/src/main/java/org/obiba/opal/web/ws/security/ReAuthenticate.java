package org.obiba.opal.web.ws.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that re-authentication is required to access the annotated resource or method, if the
 * session duration is longer than the configured timeout.
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ReAuthenticate {
}
