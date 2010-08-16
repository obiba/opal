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

import javax.ws.rs.core.Response.Status;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.opal.web.model.Ws.ClientErrorDto;
import org.obiba.opal.web.model.Ws.DatasourceParsingErrorDto;

/**
 * Utilities for handling ClientError Dtos.
 */
public class ClientErrorDtos {

  public static ClientErrorDto.Builder getErrorMessage(Status responseStatus, String errorStatus) {
    return ClientErrorDto.newBuilder().setCode(responseStatus.getStatusCode()).setStatus(errorStatus);
  }

  public static ClientErrorDto.Builder getErrorMessage(Status responseStatus, String errorStatus, MagmaRuntimeException e) {
    ClientErrorDto.Builder clientError = getErrorMessage(responseStatus, errorStatus);
    clientError.addArguments(e.getMessage());
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

  private static DatasourceParsingErrorDto.Builder newDatasourceParsingErrorDto(DatasourceParsingException pe) {
    DatasourceParsingErrorDto.Builder parsingError = DatasourceParsingErrorDto.newBuilder();
    parsingError.setDefaultMessage(pe.getMessage());
    parsingError.setKey(pe.getKey());
    for(Object arg : pe.getParameters()) {
      parsingError.addArguments(arg.toString());
    }
    return parsingError;
  }
}
