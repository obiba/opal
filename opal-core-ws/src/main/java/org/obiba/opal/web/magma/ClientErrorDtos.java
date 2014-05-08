/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

import java.util.Arrays;

import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;

import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.WrappedException;
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.opal.web.model.Magma.DatasourceParsingErrorDto;
import org.obiba.opal.web.model.Magma.JavaScriptErrorDto;
import org.obiba.opal.web.model.Ws;
import org.obiba.opal.web.model.Ws.ClientErrorDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Utilities for handling ClientError Dtos.
 */
public class ClientErrorDtos {

  private static final Logger log = LoggerFactory.getLogger(ClientErrorDtos.class);

  private ClientErrorDtos() {}

  public static ClientErrorDto.Builder getErrorMessage(Response.StatusType responseStatus, String errorStatus,
      String... args) {
    ClientErrorDto.Builder builder = ClientErrorDto.newBuilder() //
        .setCode(responseStatus.getStatusCode()) //
        .setStatus(errorStatus == null ? "" : errorStatus);

    if(args != null) {
      builder.addAllArguments(Iterables.filter(Arrays.asList(args), new Predicate<String>() {
        @Override
        public boolean apply(@Nullable String s) {
          return s != null;
        }
      }));
    }
    return builder;
  }

  @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
  public static ClientErrorDto getErrorMessage(Response.StatusType responseStatus, String errorStatus, Exception e) {
    ClientErrorDto.Builder builder = getErrorMessage(responseStatus, errorStatus);
    Throwable cause = getRootCause(e);
    builder.addArguments(cause.getMessage() == null ? cause.getClass().getName() : cause.getMessage());
    return builder.build();
  }

  @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
  public static ClientErrorDto getErrorMessage(Response.StatusType responseStatus, String errorStatus,
      RuntimeException e) {
    log.warn(errorStatus, e);
    Throwable cause = getRootCause(e);
    return getErrorMessage(responseStatus, errorStatus)
        .addArguments(cause.getMessage() == null ? cause.getClass().getName() : cause.getMessage()).build();
  }

  private static Throwable getRootCause(Exception e) {
    Throwable cause = e;
    while(cause.getCause() != null) {
      cause = cause.getCause();
    }
    return cause;
  }

  public static ClientErrorDto getErrorMessage(Response.StatusType responseStatus, String errorStatus,
      DatasourceParsingException pe) {
    ClientErrorDto.Builder builder = getErrorMessage(responseStatus, errorStatus);
    builder.addArguments(pe.getMessage());
    // build a parsing error dto list
    if(pe.getChildren().isEmpty()) {
      builder.addExtension(DatasourceParsingErrorDto.errors, newErrorDto(pe).build());
    } else {
      for(DatasourceParsingException child : pe.getChildrenAsList()) {
        builder.addExtension(DatasourceParsingErrorDto.errors, newErrorDto(child).build());
      }
    }
    return builder.build();
  }

  public static ClientErrorDto getErrorMessage(Response.StatusType responseStatus, String errorStatus,
      RhinoException exception) {
    String message = exception.getMessage();
    if(exception instanceof WrappedException)
      message = ((WrappedException) exception).getWrappedException().getMessage();
    return getErrorMessage(responseStatus, errorStatus) //
        .addArguments(message) //
        .addExtension(JavaScriptErrorDto.errors, newErrorDto(exception).build()).build();
  }

  public static ClientErrorDto getErrorMessage(Response.StatusType responseStatus, String errorStatus,
      ConstraintViolationException exception) {

    ClientErrorDto.Builder builder = getErrorMessage(responseStatus, errorStatus);
    for(ConstraintViolation<?> violation : exception.getConstraintViolations()) {
      String trimmedMessageTemplate = violation.getMessageTemplate()
          .substring(1, violation.getMessageTemplate().length() - 1);
      builder.addExtension(Ws.ConstraintViolationErrorDto.errors, Ws.ConstraintViolationErrorDto.newBuilder() //
          .setMessage(violation.getMessage()) //
          .setMessageTemplate(trimmedMessageTemplate) //
          .setPropertyPath(violation.getPropertyPath().toString()).build());
    }
    return builder.build();
  }

  private static DatasourceParsingErrorDto.Builder newErrorDto(DatasourceParsingException pe) {
    DatasourceParsingErrorDto.Builder builder = DatasourceParsingErrorDto.newBuilder() //
        .setDefaultMessage(pe.getMessage()) //
        .setKey(pe.getKey());
    for(Object arg : pe.getParameters()) {
      builder.addArguments(arg.toString());
    }
    return builder;
  }

  private static JavaScriptErrorDto.Builder newErrorDto(RhinoException exception) {
    String message = exception.details();
    if(exception instanceof WrappedException)
      message = ((WrappedException) exception).getWrappedException().getMessage();
    JavaScriptErrorDto.Builder builder = JavaScriptErrorDto.newBuilder() //
        .setMessage(message) //
        .setSourceName(exception.sourceName()) //
        .setLineNumber(exception.lineNumber()); //
    if(exception.lineSource() != null) {
      builder.setLineSource(exception.lineSource());
    }
    if(exception.columnNumber() != 0) { // column number is 0 if unknown
      builder.setColumnNumber(exception.columnNumber());
    }
    return builder;
  }

}
