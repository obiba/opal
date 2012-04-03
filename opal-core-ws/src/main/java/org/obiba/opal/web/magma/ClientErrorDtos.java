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
import java.util.Collections;

import javax.ws.rs.core.Response.Status;

import org.mozilla.javascript.RhinoException;
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.opal.web.model.Magma.DatasourceParsingErrorDto;
import org.obiba.opal.web.model.Magma.JavaScriptErrorDto;
import org.obiba.opal.web.model.Ws.ClientErrorDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for handling ClientError Dtos.
 */
public class ClientErrorDtos {

  private static final Logger log = LoggerFactory.getLogger(ClientErrorDtos.class);

  public static ClientErrorDto.Builder getErrorMessage(Status responseStatus, String errorStatus) {
    return ClientErrorDto.newBuilder().setCode(responseStatus.getStatusCode()).setStatus(errorStatus != null ? errorStatus : "");
  }

  public static ClientErrorDto.Builder getErrorMessage(Status responseStatus, String errorStatus, String... args) {
    return ClientErrorDto.newBuilder().setCode(responseStatus.getStatusCode()).setStatus(errorStatus != null ? errorStatus : "").addAllArguments(args != null ? Arrays.asList(args) : Collections.<String> emptyList());
  }

  public static ClientErrorDto.Builder getErrorMessage(Status responseStatus, String errorStatus, Exception e) {
    ClientErrorDto.Builder clientError = getErrorMessage(responseStatus, errorStatus);
    Throwable cause = e;
    while(cause.getCause() != null) {
      cause = cause.getCause();
    }
    clientError.addArguments(cause.getMessage());
    return clientError;
  }

  public static ClientErrorDto.Builder getErrorMessage(Status responseStatus, String errorStatus, RuntimeException e) {
    log.warn(errorStatus, e);
    Throwable cause = e;
    while(cause.getCause() != null) {
      cause = cause.getCause();
    }
    ClientErrorDto.Builder clientError = getErrorMessage(responseStatus, errorStatus);
    clientError.addArguments(cause.getMessage());
    return clientError;
  }

  public static ClientErrorDto.Builder getErrorMessage(Status responseStatus, String errorStatus, DatasourceParsingException pe) {
    ClientErrorDto.Builder clientError = getErrorMessage(responseStatus, errorStatus);
    clientError.addArguments(pe.getMessage());
    // build a parsing error dto list
    if(pe.getChildren().size() == 0) {
      clientError.addExtension(DatasourceParsingErrorDto.errors, newDatasourceParsingErrorDto(pe).build());
    } else {
      for(DatasourceParsingException child : pe.getChildrenAsList()) {
        clientError.addExtension(DatasourceParsingErrorDto.errors, newDatasourceParsingErrorDto(child).build());
      }
    }
    return clientError;
  }

  public static ClientErrorDto.Builder getErrorMessage(Status responseStatus, String errorStatus, RhinoException exception) {
    ClientErrorDto.Builder clientError = getErrorMessage(responseStatus, errorStatus);
    clientError.addArguments(exception.getMessage());
    clientError.addExtension(JavaScriptErrorDto.errors, newJavaScriptErrorDto(exception).build());

    return clientError;
  }

  private static DatasourceParsingErrorDto.Builder newDatasourceParsingErrorDto(DatasourceParsingException pe) {
    DatasourceParsingErrorDto.Builder parsingError = DatasourceParsingErrorDto.newBuilder();
    parsingError.setDefaultMessage(pe.getMessage());
    parsingError.setKey(pe.getKey());
    for(Object arg : pe.getParameters()) {
      parsingError.addArguments(arg.toString());
    }
    return parsingError;
  }

  private static JavaScriptErrorDto.Builder newJavaScriptErrorDto(RhinoException exception) {
    JavaScriptErrorDto.Builder javaScriptErrorDtoBuilder = JavaScriptErrorDto.newBuilder() //
    .setMessage(exception.details()) //
    .setSourceName(exception.sourceName()) //
    .setLineNumber(exception.lineNumber()); //

    if(exception.lineSource() != null) {
      javaScriptErrorDtoBuilder.setLineSource(exception.lineSource());
    }
    if(exception.columnNumber() != 0) { // column number is 0 if unknown
      javaScriptErrorDtoBuilder.setColumnNumber(exception.columnNumber());
    }

    return javaScriptErrorDtoBuilder;
  }
}
