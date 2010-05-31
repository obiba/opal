/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bouncycastle.openssl.PEMWriter;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.UnitKeyStoreService;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.UnitKeyStore;
import org.obiba.opal.web.model.Opal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
@Path("/functional-unit")
public class FunctionalUnitResource {

  private static final Logger log = LoggerFactory.getLogger(FunctionalUnitResource.class);

  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  private UnitKeyStoreService unitKeyStoreService;

  @GET
  @Path("/{unit}")
  public Opal.FunctionalUnitDto getFunctionalUnit(@PathParam("unit") String unit) {
    FunctionalUnit functionalUnit = opalRuntime.getFunctionalUnit(unit);
    if(functionalUnit == null) throw new IllegalArgumentException("Cannot find a functionnal unit with name: " + unit);

    Opal.FunctionalUnitDto.Builder fuBuilder = Opal.FunctionalUnitDto.newBuilder().//
    setName(functionalUnit.getName()). //
    setKeyVariableName(functionalUnit.getKeyVariableName());

    return fuBuilder.build();
  }

  @GET
  @Path("/{unit}/keys")
  public List<Opal.KeyPairDto> getFunctionalUnitKeyPairs(@PathParam("unit") String unit) throws KeyStoreException, IOException {
    final List<Opal.KeyPairDto> keyPairs = Lists.newArrayList();

    UnitKeyStore keystore = unitKeyStoreService.getUnitKeyStore(unit);
    for(String alias : keystore.listAliases()) {
      Opal.KeyPairDto.Builder kpBuilder = Opal.KeyPairDto.newBuilder().setAlias(alias);

      kpBuilder.setCertificate(getPEMCertificate(keystore, alias));
      keyPairs.add(kpBuilder.build());
    }

    return keyPairs;
  }

  @POST
  @Path("/{unit}/key")
  public Response createOrUpdateFunctionalUnitKeyPair(@PathParam("unit") String unit, Opal.KeyPairForm kpForm) {
    if(kpForm.hasPrivateForm() && kpForm.hasPublicForm()) {
      unitKeyStoreService.createOrUpdateKey(unit, kpForm.getAlias(), kpForm.getPrivateForm().getAlgo(), kpForm.getPrivateForm().getSize(), getCertificateInfo(kpForm.getPublicForm()));
    } else if(kpForm.hasPrivateImport()) {
      if(kpForm.hasPublicForm()) {
        throw new UnsupportedOperationException();
        // TODO
        // unitKeyStoreService.importKey(unit, kpAdd.getAlias(), kpAdd.getPrivateImport(),
        // getCertificateInfo(kpAdd.getPublicForm()));
      } else if(kpForm.hasPublicImport()) {
        throw new UnsupportedOperationException();
        // TODO
        // unitKeyStoreService.importKey(unit, kpAdd.getAlias(), kpAdd.getPrivateImport(), kpAdd.getPublicImport());
      } else {
        throw new IllegalArgumentException("Missing information about public key for alias: " + kpForm.getAlias());
      }
    } else {
      throw new IllegalArgumentException("Missing information about private key for alias: " + kpForm.getAlias());
    }

    return Response.ok().build();
  }

  @DELETE
  @Path("/{unit}/key/{alias}")
  public Response deleteFunctionalUnitKeyPair(@PathParam("unit") String unit, @PathParam("alias") String alias) {
    unitKeyStoreService.deleteKey(unit, alias);

    return Response.ok().build();
  }

  @GET
  @Path("/{unit}/key/{alias}/certificate")
  public Response getFunctionalUnitKeyPairCertificate(@PathParam("unit") String unit, @PathParam("alias") String alias) throws KeyStoreException, IOException {
    UnitKeyStore keystore = unitKeyStoreService.getUnitKeyStore(unit);

    return Response.ok(getPEMCertificate(keystore, alias), MediaType.TEXT_PLAIN_TYPE).header("Content-disposition", "attachment; filename=\"" + unit + "-" + alias + "-certificate.pem\"").build();
  }

  private String getPEMCertificate(UnitKeyStore keystore, String alias) throws KeyStoreException, IOException {
    Certificate certificate = keystore.getKeyStore().getCertificate(alias);
    if(certificate == null) throw new IllegalArgumentException("Cannot find certificate for alias: " + alias);

    StringWriter writer = new StringWriter();
    PEMWriter pemWriter = new PEMWriter(writer);
    pemWriter.writeObject(certificate);
    pemWriter.flush();
    return writer.getBuffer().toString();
  }

  private String getCertificateInfo(Opal.PublicKeyForm pkForm) {
    return "CN=" + pkForm.getName() + ", OU=" + pkForm.getOrganizationalUnit() + ", O=" + pkForm.getOrganization() + ", L=" + pkForm.getLocality() + ", ST=" + pkForm.getState() + ", C=" + pkForm.getCountry();
  }

}
