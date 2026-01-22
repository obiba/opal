/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.security;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.obiba.opal.core.service.security.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/system/crypto")
@Tag(name = "System", description = "Operations on system administration")
public class CryptoResource {

  @Autowired
  private CryptoService cryptoService;

  @GET
  @Path("/encrypt/{plain}")
  @Operation(
    summary = "Encrypt text",
    description = "Encrypts the provided plain text using the system's configured encryption service. Returns the encrypted text that can be safely stored or transmitted."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Text successfully encrypted"),
    @ApiResponse(responseCode = "400", description = "Invalid input text"),
    @ApiResponse(responseCode = "500", description = "Encryption service error")
  })
  public String encrypt(@PathParam("plain") String plain) {
    return cryptoService.encrypt(plain);
  }

  @GET
  @Path("/decrypt/{encrypted}")
  @Operation(
    summary = "Decrypt text",
    description = "Decrypts the provided encrypted text using the system's configured encryption service. Returns the original plain text."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Text successfully decrypted"),
    @ApiResponse(responseCode = "400", description = "Invalid encrypted text format"),
    @ApiResponse(responseCode = "500", description = "Decryption service error")
  })
  public String decrypt(@PathParam("encrypted") String encrypted) {
    return cryptoService.decrypt(encrypted);
  }

}
