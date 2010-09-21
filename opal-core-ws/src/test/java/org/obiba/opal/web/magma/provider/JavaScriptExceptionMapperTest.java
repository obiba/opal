/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.mozilla.javascript.JavaScriptException;
import org.obiba.opal.web.model.Magma.JavaScriptErrorDto;
import org.obiba.opal.web.model.Ws.ClientErrorDto;

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
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
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
    assertNotNull(entity);
    assertTrue(entity instanceof ClientErrorDto);
    ClientErrorDto clientErrorDto = (ClientErrorDto) entity;
    assertEquals(1, clientErrorDto.getExtensionCount(JavaScriptErrorDto.errors));
    JavaScriptErrorDto jsErrorDto = clientErrorDto.getExtension(JavaScriptErrorDto.errors, 0);
    assertEquals("sourceName", jsErrorDto.getSourceName());
    assertEquals(1, jsErrorDto.getLineNumber());
  }
}
