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

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.obiba.opal.core.service.security.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/system/crypto")
public class CryptoResource {

  @Autowired
  private CryptoService cryptoService;

  @GET
  @Path("/encrypt/{plain}")
  public String encrypt(@PathParam("plain") String plain) {
    return cryptoService.encrypt(plain);
  }

  @GET
  @Path("/decrypt/{encrypted}")
  public String decrypt(@PathParam("encrypted") String encrypted) {
    return cryptoService.decrypt(encrypted);
  }

}
