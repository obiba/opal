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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.bouncycastle.openssl.PEMWriter;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.crypt.MagmaCryptRuntimeException;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.StaticDatasource;
import org.obiba.magma.support.StaticValueTable;
import org.obiba.opal.core.domain.participant.identifier.impl.DefaultParticipantIdentifierImpl;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.core.service.ImportService;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.service.UnitKeyStoreService;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.FunctionalUnitIdentifierMapper;
import org.obiba.opal.core.unit.FunctionalUnitIdentifiers;
import org.obiba.opal.core.unit.FunctionalUnitIdentifiers.UnitIdentifier;
import org.obiba.opal.core.unit.FunctionalUnitService;
import org.obiba.opal.core.unit.IllegalIdentifierAssociationException;
import org.obiba.opal.core.unit.UnitKeyStore;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.magma.support.DatasourceFactoryRegistry;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.VariableEntityDto;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Opal.EntryDto;
import org.obiba.opal.web.model.Opal.KeyType;
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Component
@Scope("request")
@Path("/functional-unit/{unit}")
public class FunctionalUnitResource extends AbstractFunctionalUnitResource {

  private static final Logger log = LoggerFactory.getLogger(FunctionalUnitResource.class);

  private final FunctionalUnitService functionalUnitService;

  private final OpalRuntime opalRuntime;

  private final UnitKeyStoreService unitKeyStoreService;

  private final ImportService importService;

  private final DatasourceFactoryRegistry datasourceFactoryRegistry;

  private final IdentifiersTableService identifiersTableService;

  @PathParam("unit")
  private String unit;

  @Autowired
  public FunctionalUnitResource(FunctionalUnitService functionalUnitService, OpalRuntime opalRuntime, UnitKeyStoreService unitKeyStoreService, ImportService importService, DatasourceFactoryRegistry datasourceFactoryRegistry, IdentifiersTableService identifiersTableService) {
    this.functionalUnitService = functionalUnitService;
    this.opalRuntime = opalRuntime;
    this.unitKeyStoreService = unitKeyStoreService;
    this.importService = importService;
    this.datasourceFactoryRegistry = datasourceFactoryRegistry;
    this.identifiersTableService = identifiersTableService;
  }

  //
  // Functional Unit
  //

  @GET
  public Opal.FunctionalUnitDto getFunctionalUnit() {
    FunctionalUnit functionalUnit = resolveFunctionalUnit(unit);

    Opal.FunctionalUnitDto.Builder fuBuilder = Opal.FunctionalUnitDto.newBuilder().//
    setName(functionalUnit.getName()). //
    setKeyVariableName(functionalUnit.getKeyVariableName());
    if(functionalUnit.getSelect() != null && functionalUnit.getSelect() instanceof JavascriptClause) {
      fuBuilder.setSelect(((JavascriptClause) functionalUnit.getSelect()).getScript());
    }

    return fuBuilder.build();
  }

  @PUT
  public Response createOrUpdateFunctionalUnit(@Context
  UriInfo uri, Opal.FunctionalUnitDto unitDto) {
    if(!unit.equals(unitDto.getName())) {
      return Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "WrongFunctionalUnitArgument").build()).build();
    }

    ResponseBuilder response = null;
    try {
      FunctionalUnit functionalUnit = new FunctionalUnit(unitDto.getName(), unitDto.getKeyVariableName());
      if (unitDto.hasDescription()) {
        functionalUnit.setDescription(unitDto.getDescription());
      }
      if(unitDto.hasSelect()) {
        functionalUnit.setSelect(new JavascriptClause(unitDto.getSelect()));
      }
      functionalUnit.setUnitKeyStoreService(unitKeyStoreService);

      if(getFunctionalUnitService().hasFunctionalUnit(unitDto.getName())) {
        response = Response.ok();
      } else {
        response = Response.created(uri.getAbsolutePath());
      }

      getFunctionalUnitService().addOrReplaceFunctionalUnit(functionalUnit);

    } catch(RuntimeException e) {
      response = Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "FunctionalUnitCreationFailed", e).build());
    }

    return response.build();
  }

  @DELETE
  public Response removeUnit() {
    if(!getFunctionalUnitService().hasFunctionalUnit(unit)) {
      return Response.status(Status.NOT_FOUND).build();
    }
    getFunctionalUnitService().removeFunctionalUnit(unit);
    // TODO: delete the units variable in the keys table

    return Response.ok().build();
  }

  //
  // Entities
  //

  @GET
  @Path("/entities")
  public Iterable<VariableEntityDto> getEntities() {
    return getUnitEntities();
  }

  @GET
  @Path("/entities/count")
  public String getEntitiesCount() {
    return String.valueOf(Iterables.size(getUnitEntities()));
  }

  @GET
  @Path("/entities/identifiers")
  @Produces("text/plain")
  @AuthenticatedByCookie
  public Response getIdentifiers() {
    ByteArrayOutputStream ids = new ByteArrayOutputStream();
    final PrintWriter writer = new PrintWriter(ids);
    readUnitIdentifiers(new VectorCallback() {

      @Override
      public void onValue(UnitIdentifier unitIdentifier) {
        if(unitIdentifier.hasUnitIdentifier()) {
          writer.append(unitIdentifier.getUnitIdentifier()).append("\n");
        }
      }

    });

    writer.close();
    return Response.ok(ids.toByteArray(), MediaType.TEXT_PLAIN).header("Content-Disposition", "attachment; filename=\"" + unit + "-identifiers.txt\"").build();
  }

  @GET
  @Path("/entities/csv")
  @Produces("text/csv")
  @AuthenticatedByCookie
  public Response getCSVIdentifiers() throws MagmaRuntimeException, IOException {
    ByteArrayOutputStream ids = new ByteArrayOutputStream();
    final PrintWriter writer = new PrintWriter(ids);
    writer.append('"').append(FunctionalUnit.OPAL_INSTANCE).append("\",\"").append(unit).append("\"\n");
    readUnitIdentifiers(new VectorCallback() {

      @Override
      public void onValue(UnitIdentifier unitIdentifier) {
        if(unitIdentifier.hasUnitIdentifier()) {
          writer.append("\"").append(unitIdentifier.getOpalIdentifier()).append("\",")//
          .append(unitIdentifier.hasUnitIdentifier() ? "\"" + unitIdentifier.getUnitIdentifier() + "\"" : "").append("\n");
        }
      }

    });

    writer.close();
    return Response.ok(ids.toByteArray(), "text/csv").header("Content-Disposition", "attachment; filename=\"" + unit + "-identifiers.csv\"").build();
  }

  @POST
  @Path("/entities")
  public Response importIdentifiers(DatasourceFactoryDto datasourceFactoryDto, @QueryParam("select")
  String select) {
    Response response = null;

    Datasource sourceDatasource = createTransientDatasource(datasourceFactoryDto);

    try {
      importService.importIdentifiers(unit, sourceDatasource.getName(), select);
      response = Response.ok().build();
    } catch(NoSuchDatasourceException ex) {
      response = Response.status(Status.NOT_FOUND).entity(ClientErrorDtos.getErrorMessage(Status.NOT_FOUND, "DatasourceNotFound", ex).build()).build();
    } catch(NoSuchValueTableException ex) {
      response = Response.status(Status.NOT_FOUND).entity(ClientErrorDtos.getErrorMessage(Status.NOT_FOUND, "ValueTableNotFound", ex).build()).build();
    } catch(IOException ex) {
      response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(ClientErrorDtos.getErrorMessage(Status.INTERNAL_SERVER_ERROR, "DatasourceCopierIOException", ex).build()).build();
    } finally {
      Disposables.silentlyDispose(sourceDatasource);
    }

    return response;
  }

  @POST
  @Path("/entities/identifiers")
  public Response importIdentifiers(@QueryParam("size")
  Integer size, @QueryParam("zeros")
  Boolean zeros, @QueryParam("prefix")
  String prefix) {
    try {
      DefaultParticipantIdentifierImpl pId = new DefaultParticipantIdentifierImpl();
      if(size != null) pId.setKeySize(size);
      if(zeros != null) pId.setAllowStartWithZero(zeros);
      if(prefix != null) pId.setPrefix(prefix);
      int count = importService.importIdentifiers(unit, pId);
      return Response.ok().entity(Integer.toString(count)).build();
    } catch(NoSuchDatasourceException ex) {
      return Response.status(Status.NOT_FOUND).entity(ClientErrorDtos.getErrorMessage(Status.NOT_FOUND, "DatasourceNotFound", ex).build()).build();
    } catch(NoSuchValueTableException ex) {
      return Response.status(Status.NOT_FOUND).entity(ClientErrorDtos.getErrorMessage(Status.NOT_FOUND, "ValueTableNotFound", ex).build()).build();
    } catch(MagmaRuntimeException ex) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ClientErrorDtos.getErrorMessage(Status.INTERNAL_SERVER_ERROR, "ImportIdentifiersError", ex).build()).build();
    }
  }

  @PUT
  @Path("/entities/identifiers/map")
  public Response mapIdentifiers(@QueryParam("path")
  String path, @QueryParam("create")
  @DefaultValue("false")
  boolean create) {
    try {
      // the file is expected to be of CSV format
      File mapFile = resolveLocalFile(path);
      CSVReader reader = new CSVReader(new FileReader(mapFile.getPath()));
      List<FunctionalUnit> units = getUnitsFromIdentifiersMap(reader);
      FunctionalUnit drivingUnit = determineDrivingUnit(units);
      int unitIdx = units.indexOf(drivingUnit);
      List<String[]> rows = (List<String[]>) reader.readAll();

      // make sure all entities exist
      if(create) {
        prepareMapIdentifiers(drivingUnit, rows, unitIdx);
      }

      return Response.ok().entity(Integer.toString(doMapIdentifiers(drivingUnit, units, rows, unitIdx))).build();
    } catch(Exception e) {
      log.error("Mapping failed", e);
      return Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "mappingFailed", e).build()).build();
    }
  }

  private void prepareMapIdentifiers(FunctionalUnit drivingUnit, List<String[]> rows, int unitIdx) throws NoSuchValueTableException, IOException {
    ImmutableSet.Builder<String> ids = ImmutableSet.builder();
    for(String[] map : rows) {
      if(map[unitIdx] != null && map[unitIdx].isEmpty() == false) {
        ids.add(map[unitIdx]);
      }
    }
    importIdentifiers(drivingUnit, ids.build());
  }

  private int doMapIdentifiers(FunctionalUnit drivingUnit, List<FunctionalUnit> units, List<String[]> rows, int unitIdx) throws IOException {
    FunctionalUnitIdentifierMapper mapper = new FunctionalUnitIdentifierMapper(identifiersTableService.getValueTable(), drivingUnit, units);
    int count = 0;
    for(String[] map : rows) {
      if(map[unitIdx] != null && map[unitIdx].isEmpty() == false) {
        count += mapper.associate(map[unitIdx], units, map);
      }
    }
    mapper.write();
    return count;
  }

  @PUT
  @Path("/entities/identifiers/map/functional-unit/{toUnit}")
  public Response mapIdentifiers(List<EntryDto> identifiers, @PathParam("toUnit")
  String toUnit, @QueryParam("create")
  @DefaultValue("false")
  boolean create) {
    try {
      List<FunctionalUnit> units = getUnitsFromName(unit, toUnit);
      FunctionalUnit drivingUnit = determineDrivingUnit(units);

      // make sure all entities exist
      if(create) {
        prepareMapIdentifiers(drivingUnit, identifiers);
      }

      return Response.ok().entity(Integer.toString(doMapIdentifiers(drivingUnit, units, identifiers))).build();
    } catch(Exception e) {
      log.error("Mapping failed", e);
      return Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "mappingFailed", e).build()).build();
    }
  }

  private void prepareMapIdentifiers(FunctionalUnit drivingUnit, List<EntryDto> identifiers) throws NoSuchValueTableException, IOException {
    ImmutableSet.Builder<String> ids = ImmutableSet.builder();
    for(EntryDto entry : identifiers) {
      ids.add(entry.getKey());
    }
    importIdentifiers(drivingUnit, ids.build());
  }

  private int doMapIdentifiers(FunctionalUnit drivingUnit, List<FunctionalUnit> units, List<EntryDto> identifiers) throws IOException {
    FunctionalUnitIdentifierMapper mapper = new FunctionalUnitIdentifierMapper(identifiersTableService.getValueTable(), drivingUnit, units);
    int count = 0;
    for(EntryDto entry : identifiers) {
      count += mapper.associate(entry.getKey(), units, new String[] { entry.getKey(), entry.getValue() });
    }
    mapper.write();
    return count;
  }

  //
  // Keystore
  //

  @GET
  @Path("/keys")
  public List<Opal.KeyDto> getFunctionalUnitKeyPairs() throws KeyStoreException, IOException {
    final List<Opal.KeyDto> keyPairs = Lists.newArrayList();

    UnitKeyStore keystore = unitKeyStoreService.getUnitKeyStore(unit);
    if(keystore != null) {
      for(String alias : keystore.listAliases()) {
        Opal.KeyType type = Opal.KeyType.valueOf(keystore.getKeyType(alias).toString());
        Opal.KeyDto.Builder kpBuilder = Opal.KeyDto.newBuilder().setAlias(alias).setKeyType(type);

        kpBuilder.setCertificate(getPEMCertificate(keystore, alias));
        keyPairs.add(kpBuilder.build());
      }

      sortByName(keyPairs);
    }

    return keyPairs;
  }

  @POST
  @Path("/keys")
  public Response createFunctionalUnitKeyPair(Opal.KeyForm kpForm) {
    if(unitKeyStoreService.aliasExists(unit, kpForm.getAlias())) {
      return Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "KeyPairAlreadyExists").build()).build();
    }

    ResponseBuilder response = null;
    try {
      if(kpForm.getKeyType() == KeyType.KEY_PAIR) {
        response = doCreateOrImportKeyPair(kpForm);
      } else {
        response = doImportCertificate(kpForm);
      }
      if(response == null) {
        response = Response.created(UriBuilder.fromPath("/").path(FunctionalUnitResource.class).path("/key/" + kpForm.getAlias()).build(unit));
      }
    } catch(Exception e) {
      response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(ClientErrorDtos.getErrorMessage(Status.INTERNAL_SERVER_ERROR, "KeyPairCreationFailed", e).build());
    }

    return response.build();
  }

  @DELETE
  @Path("/key/{alias}")
  public Response deleteFunctionalUnitKeyPair(@PathParam("alias")
  String alias) {
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
  @AuthenticatedByCookie
  public Response getFunctionalUnitKeyPairCertificate(@PathParam("alias")
  String alias) throws KeyStoreException, IOException {
    UnitKeyStore keystore = unitKeyStoreService.getUnitKeyStore(unit);

    return Response.ok(getPEMCertificate(keystore, alias), MediaType.TEXT_PLAIN_TYPE).header("Content-disposition", "attachment; filename=\"" + unit + "-" + alias + "-certificate.pem\"").build();
  }

  //
  // Private methods
  //

  private void importIdentifiers(FunctionalUnit drivingUnit, Set<String> ids) throws NoSuchValueTableException, IOException {
    StaticDatasource ds = new StaticDatasource("ids-import");
    StaticValueTable vt = new StaticValueTable(ds, identifiersTableService.getTableName(), ids, identifiersTableService.getEntityType());
    ds.addValueTable(vt);
    if(drivingUnit.getName().equals(FunctionalUnit.OPAL_INSTANCE)) {
      importService.importIdentifiers(ds);
    } else {
      importService.importIdentifiers(drivingUnit, ds, null);
    }
  }

  private ResponseBuilder doImportCertificate(Opal.KeyForm kpForm) {
    try {
      unitKeyStoreService.importCertificate(this.unit, kpForm.getAlias(), new ByteArrayInputStream(kpForm.getPublicImport().getBytes()));
    } catch(MagmaCryptRuntimeException e) {
      return Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "InvalidCertificate").build());
    }
    return null;
  }

  private ResponseBuilder doCreateOrImportKeyPair(Opal.KeyForm kpForm) {
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

  private ResponseBuilder doImportKeyPair(Opal.KeyForm kpForm) {
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

  private Datasource createTransientDatasource(DatasourceFactoryDto datasourceFactoryDto) {
    DatasourceFactory factory = datasourceFactoryRegistry.parse(datasourceFactoryDto);
    String uid = MagmaEngine.get().addTransientDatasource(factory);

    return MagmaEngine.get().getTransientDatasourceInstance(uid);
  }

  private Iterable<VariableEntityDto> getUnitEntities() {
    return Iterables.transform(new FunctionalUnitIdentifiers(identifiersTableService.getValueTable(), resolveFunctionalUnit(unit)).getUnitEntities(), Dtos.variableEntityAsDtoFunc);
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

  private void sortByName(List<Opal.KeyDto> units) {
    // sort alphabetically
    Collections.sort(units, new Comparator<Opal.KeyDto>() {

      @Override
      public int compare(Opal.KeyDto d1, Opal.KeyDto d2) {
        return d1.getAlias().compareTo(d2.getAlias());
      }

    });
  }

  private void readUnitIdentifiers(final VectorCallback callback) {
    ValueTable keysTable = identifiersTableService.getValueTable();
    FunctionalUnit functionalUnit = resolveFunctionalUnit(unit);
    if(keysTable.hasVariable(functionalUnit.getKeyVariableName())) {
      for(UnitIdentifier unitId : new FunctionalUnitIdentifiers(keysTable, functionalUnit)) {
        // entities of the unit are the ones that have a non null for the unit identifier variable
        callback.onValue(unitId);
      }
    }
  }

  private FunctionalUnit determineDrivingUnit(List<FunctionalUnit> units) {
    FunctionalUnit drivingUnit;
    try {
      drivingUnit = resolveFunctionalUnit(this.unit);
      // When the driving unit is not Opal, the Opal unit cannot be present in the list of functional units
      if(units.contains(FunctionalUnit.OPAL)) {
        throw new IllegalIdentifierAssociationException("Cannot create Opal identifiers through this function.");
      }
    } catch(NoSuchFunctionalUnitException e) {
      if(this.unit.equals(FunctionalUnit.OPAL_INSTANCE)) {
        drivingUnit = FunctionalUnit.OPAL;
      } else {
        throw e;
      }
    }
    return drivingUnit;
  }

  private interface VectorCallback {

    public void onValue(UnitIdentifier unitIdentifier);

  }

  @Override
  protected OpalRuntime getOpalRuntime() {
    return opalRuntime;
  }

  @Override
  protected FunctionalUnitService getFunctionalUnitService() {
    return functionalUnitService;
  }

}
