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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.bouncycastle.openssl.PEMWriter;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.UnitKeyStoreService;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.UnitKeyStore;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.magma.TableResource;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Magma.ValueSetDto;
import org.obiba.opal.web.model.Magma.VariableEntityDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

@Component
@Scope("request")
@Path("/functional-unit/{unit}")
public class FunctionalUnitResource {

  private static final Logger log = LoggerFactory.getLogger(FunctionalUnitResource.class);

  private final OpalRuntime opalRuntime;

  private final UnitKeyStoreService unitKeyStoreService;

  private final String keysDatasourceName;

  private final String keysTableName;

  @PathParam("unit")
  private String unit;

  @Autowired
  public FunctionalUnitResource(OpalRuntime opalRuntime, UnitKeyStoreService unitKeyStoreService, @Value("${org.obiba.opal.keys.tableReference}") String keysTableReference) {
    this.opalRuntime = opalRuntime;
    this.unitKeyStoreService = unitKeyStoreService;
    MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf(keysTableReference);
    this.keysDatasourceName = resolver.getDatasourceName();
    if(keysDatasourceName == null) {
      throw new IllegalArgumentException("invalid keys table reference");
    }
    this.keysTableName = resolver.getTableName();
    if(keysTableName == null) {
      throw new IllegalArgumentException("invalid keys table reference");
    }
  }

  @GET
  public Response getFunctionalUnit() {
    FunctionalUnit functionalUnit = opalRuntime.getFunctionalUnit(unit);
    if(functionalUnit == null) return Response.status(Status.NOT_FOUND).build();

    Opal.FunctionalUnitDto.Builder fuBuilder = Opal.FunctionalUnitDto.newBuilder().//
    setName(functionalUnit.getName()). //
    setKeyVariableName(functionalUnit.getKeyVariableName());
    if(functionalUnit.getSelect() != null && functionalUnit.getSelect() instanceof JavascriptClause) {
      fuBuilder.setSelect(((JavascriptClause) functionalUnit.getSelect()).getScript());
    }

    return Response.ok(fuBuilder.build()).build();
  }

  @PUT
  public Response createOrUpdateFunctionalUnit(@Context UriInfo uri, Opal.FunctionalUnitDto unitDto) {
    if(!unit.equals(unitDto.getName())) {
      return Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "WrongFunctionalUnitArgument").build()).build();
    }

    ResponseBuilder response = null;
    try {
      FunctionalUnit functionalUnit = new FunctionalUnit(unitDto.getName(), unitDto.getKeyVariableName());
      if(unitDto.hasSelect()) {
        functionalUnit.setSelect(new JavascriptClause(unitDto.getSelect()));
      }
      functionalUnit.setUnitKeyStoreService(unitKeyStoreService);

      if(opalRuntime.getOpalConfiguration().hasFunctionalUnit(unitDto.getName())) {
        response = Response.ok();
      } else {
        response = Response.created(uri.getAbsolutePath());
      }

      opalRuntime.getOpalConfiguration().addOrReplaceFunctionalUnit(functionalUnit);
      opalRuntime.writeOpalConfiguration();

    } catch(RuntimeException e) {
      response = Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "FunctionalUnitCreationFailed", e).build());
    }

    return response.build();
  }

  @DELETE
  public Response removeUnit() {
    if(!opalRuntime.getOpalConfiguration().hasFunctionalUnit(unit)) {
      return Response.status(Status.NOT_FOUND).build();
    }
    opalRuntime.getOpalConfiguration().removeFunctionalUnit(unit);
    opalRuntime.writeOpalConfiguration();

    return Response.ok().build();
  }

  @GET
  @Path("/entities")
  public Set<VariableEntityDto> getEntities() {
    ValueTable keysTable = MagmaEngine.get().getDatasource(keysDatasourceName).getValueTable(keysTableName);
    TableResource tableResource = new TableResource(keysTable);

    FunctionalUnit functionalUnit = opalRuntime.getFunctionalUnit(unit);
    if(functionalUnit == null) throw new IllegalArgumentException("No such unit: " + unit);

    Set<VariableEntityDto> entities;
    if(keysTable.hasVariable(functionalUnit.getKeyVariableName())) {
      entities = tableResource.getEntities("$('" + functionalUnit.getKeyVariableName() + "').isNull().not()");
    } else {
      entities = new ImmutableSet.Builder<VariableEntityDto>().build();
    }

    return entities;
  }

  @GET
  @Path("/entities/count")
  public Response getEntitiesCount() {
    return Response.ok(String.valueOf(getEntities().size())).build();
  }

  @GET
  @Path("/entities/identifiers")
  @Produces("text/plain")
  public Response getIdentifiers() {
    ValueTable keysTable = MagmaEngine.get().getDatasource(keysDatasourceName).getValueTable(keysTableName);
    TableResource tableResource = new TableResource(keysTable);

    FunctionalUnit functionalUnit = opalRuntime.getFunctionalUnit(unit);
    if(functionalUnit == null) throw new IllegalArgumentException("No such unit: " + unit);

    ByteArrayOutputStream ids = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(ids);
    if(keysTable.hasVariable(functionalUnit.getKeyVariableName())) {
      Collection<ValueSetDto> values = tableResource.getValueSets("name().value()=='" + functionalUnit.getKeyVariableName() + "'", "$('" + functionalUnit.getKeyVariableName() + "').isNull().not()", 0, Integer.MAX_VALUE);
      for(ValueSetDto value : values) {
        if(value.getVariablesCount() == 1) writer.append(value.getValues(0).getValue()).append("\n");
      }
    }
    writer.close();

    return Response.ok(ids.toByteArray(), MediaType.TEXT_PLAIN).header("Content-Disposition", "attachment; filename=\"" + unit + "-identifiers.txt\"").build();
  }

  @GET
  @Path("/keys")
  public List<Opal.KeyPairDto> getFunctionalUnitKeyPairs() throws KeyStoreException, IOException {
    final List<Opal.KeyPairDto> keyPairs = Lists.newArrayList();

    UnitKeyStore keystore = unitKeyStoreService.getUnitKeyStore(unit);
    if(keystore != null) {
      for(String alias : keystore.listAliases()) {
        Opal.KeyPairDto.Builder kpBuilder = Opal.KeyPairDto.newBuilder().setAlias(alias);

        kpBuilder.setCertificate(getPEMCertificate(keystore, alias));
        keyPairs.add(kpBuilder.build());
      }

      sortByName(keyPairs);
    }

    return keyPairs;
  }

  @POST
  @Path("/keys")
  public Response createFunctionalUnitKeyPair(Opal.KeyPairForm kpForm) {
    if(unitKeyStoreService.aliasExists(unit, kpForm.getAlias())) {
      return Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "KeyPairAlreadyExists").build()).build();
    }

    ResponseBuilder response = null;
    try {
      response = doCreateOrImportKeyPair(kpForm);
      if(response == null) {
        response = Response.created(UriBuilder.fromPath("/").path(FunctionalUnitResource.class).path("/key/" + kpForm.getAlias()).build(unit));
      }
    } catch(Exception e) {
      response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(ClientErrorDtos.getErrorMessage(Status.INTERNAL_SERVER_ERROR, "KeyPairCreationFailed", e).build());
    }

    return response.build();
  }

  private ResponseBuilder doCreateOrImportKeyPair(Opal.KeyPairForm kpForm) {
    ResponseBuilder response = null;
    if(kpForm.hasPrivateForm() && kpForm.hasPublicForm()) {
      unitKeyStoreService.createOrUpdateKey(unit, kpForm.getAlias(), kpForm.getPrivateForm().getAlgo(), kpForm.getPrivateForm().getSize(), getCertificateInfo(kpForm.getPublicForm()));
    } else if(kpForm.hasPrivateImport()) {
      response = doImportKeyPair(kpForm);
    } else {
      response = Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "MissingPrivateKeyArgument").build());
    }
    return response;
  }

  private ResponseBuilder doImportKeyPair(Opal.KeyPairForm kpForm) {
    ResponseBuilder response = null;
    if(kpForm.hasPublicForm()) {
      unitKeyStoreService.importKey(unit, kpForm.getAlias(), new ByteArrayInputStream(kpForm.getPrivateImport().getBytes()), getCertificateInfo(kpForm.getPublicForm()));
    } else if(kpForm.hasPublicImport()) {
      unitKeyStoreService.importKey(unit, kpForm.getAlias(), new ByteArrayInputStream(kpForm.getPrivateImport().getBytes()), new ByteArrayInputStream(kpForm.getPublicImport().getBytes()));
    } else {
      response = Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "MissingPublicKeyArgument").build());
    }
    return response;
  }

  @DELETE
  @Path("/key/{alias}")
  public Response deleteFunctionalUnitKeyPair(@PathParam("alias") String alias) {
    if(!unitKeyStoreService.aliasExists(unit, alias)) {
      return Response.status(Status.NOT_FOUND).build();
    }

    ResponseBuilder response = null;
    try {
      unitKeyStoreService.deleteKey(unit, alias);
      response = Response.ok();
    } catch(RuntimeException e) {
      response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(ClientErrorDtos.getErrorMessage(Status.INTERNAL_SERVER_ERROR, "DeleteKeyPairFailed", e).build());
    }

    return response.build();
  }

  @GET
  @Path("/key/{alias}/certificate")
  public Response getFunctionalUnitKeyPairCertificate(@PathParam("alias") String alias) throws KeyStoreException, IOException {
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

  private void sortByName(List<Opal.KeyPairDto> units) {
    // sort alphabetically
    Collections.sort(units, new Comparator<Opal.KeyPairDto>() {

      @Override
      public int compare(Opal.KeyPairDto d1, Opal.KeyPairDto d2) {
        return d1.getAlias().compareTo(d2.getAlias());
      }

    });
  }

}
