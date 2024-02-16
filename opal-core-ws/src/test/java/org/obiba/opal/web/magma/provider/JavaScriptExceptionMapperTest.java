/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma.provider;

import jakarta.ws.rs.core.Response;

import org.junit.Test;
import org.mozilla.javascript.JavaScriptException;
import org.obiba.opal.web.model.Magma.JavaScriptErrorDto;
import org.obiba.opal.web.model.Ws.ClientErrorDto;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Unit tests for {@link JavaScriptExceptionMapper}.
 */
public class JavaScriptExceptionMapperTest {
  //
  // Test Methods
  //

  @Test
  public void testToResponse_ResponseStatusIsBadRequest() {
    // Setup
    JavaScriptException exception = new JavaScriptException(null, "sourceName", 1);

    // Exercise
    JavaScriptExceptionMapper sut = new JavaScriptExceptionMapper();
    Response response = sut.toResponse(exception);

    // Verify
    assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
  }

  @Test
  public void testToResponse_ResponseEntityIsClientErrorDtoContainingJavaScriptErrorDto() {
    // Setup
    JavaScriptException exception = new JavaScriptException(null, "sourceName", 1);

    // Exercise
    JavaScriptExceptionMapper sut = new JavaScriptExceptionMapper();
    Response response = sut.toResponse(exception);

    // Verify
    Object entity = response.getEntity();
    assertThat(entity).isNotNull();
    assertThat(entity).isInstanceOf(ClientErrorDto.class);
    ClientErrorDto clientErrorDto = (ClientErrorDto) entity;
    assertThat(clientErrorDto.getExtensionCount(JavaScriptErrorDto.errors)).isEqualTo(1);
    JavaScriptErrorDto jsErrorDto = clientErrorDto.getExtension(JavaScriptErrorDto.errors, 0);
    assertThat(jsErrorDto.getSourceName()).isEqualTo("sourceName");
    assertThat(jsErrorDto.getLineNumber()).isEqualTo(1);
  }
}
