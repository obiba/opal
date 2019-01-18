/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.genotypes;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationTerminatedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.presenter.TablePresenter;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.ResourcePermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionRequestPaths;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.project.genotypes.event.VcfFileExportRequestEvent;
import org.obiba.opal.web.gwt.app.client.project.genotypes.event.VcfFileUploadRequestEvent;
import org.obiba.opal.web.gwt.app.client.project.genotypes.event.VcfMappingDeleteRequestEvent;
import org.obiba.opal.web.gwt.app.client.project.genotypes.event.VcfMappingEditRequestEvent;
import org.obiba.opal.web.gwt.app.client.project.view.ProjectPresenter;
import org.obiba.opal.web.gwt.app.client.support.FilterHelper;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.identifiers.IdentifiersMappingDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.google.gwt.http.client.Response.*;

public class ProjectGenotypesPresenter extends PresenterWidget<ProjectGenotypesPresenter.Display>
    implements ProjectGenotypesUiHandlers {

  private static final String ENTITY_TYPE_PARAM = "entityType";

  private static final String MAPPINGS_ENTITY_TYPE_PARAM = "type";

  private static final String FILE_PARAM = "file";

  private static final String PARTICIPANT_ENTITY_TYPE = "Participant";

  private static Logger logger = Logger.getLogger("ProjectGenotypesPresenter");

  private final PlaceManager placeManager;

  private ProjectDto projectDto;

  private final TranslationMessages translationMessages;

  private final ModalProvider<ProjectImportVcfFileModalPresenter> vcfFileUploadModalPresenterModalProvider;

  private final ModalProvider<ProjectExportVcfFileModalPresenter> vcfFileDownloadModalPresenterModalProvider;

  private final ModalProvider<ProjectGenotypeEditMappingTableModalPresenter> projectGenotypeEditMappingTableModalPresenterModalProvider;

  private final Provider<ResourcePermissionsPresenter> resourcePermissionsProvider;

  private JsArray<TableDto> participantTables = JsArrays.create();

  private List<String> participantIdentifiersMappingList = new ArrayList<>();

  private VCFSamplesMappingDto mappingTable = null;

  private JsArray<VCFSummaryDto> vcfSummaryDtos = JsArrays.create();

  private Runnable actionRequiringConfirmation;

  @Inject
  public ProjectGenotypesPresenter(Display display, EventBus eventBus,
                                   ModalProvider<ProjectImportVcfFileModalPresenter> vcfFileUploadModalPresenterModalProvider,
                                   ModalProvider<ProjectExportVcfFileModalPresenter> vcfFileDownloadModalPresenterModalProvider,
                                   ModalProvider<ProjectGenotypeEditMappingTableModalPresenter> editMappingTableModalPresenterModalProvider,
                                   Provider<ResourcePermissionsPresenter> resourcePermissionsProvider,
                                   TranslationMessages translationMessages,
                                   PlaceManager placeManager) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    this.translationMessages = translationMessages;
    this.vcfFileUploadModalPresenterModalProvider = vcfFileUploadModalPresenterModalProvider.setContainer(this);
    this.vcfFileDownloadModalPresenterModalProvider = vcfFileDownloadModalPresenterModalProvider.setContainer(this);
    projectGenotypeEditMappingTableModalPresenterModalProvider = editMappingTableModalPresenterModalProvider.setContainer(this);
    this.resourcePermissionsProvider = resourcePermissionsProvider;
    this.placeManager = placeManager;
    initializeEventListeners();
  }

  private void initializeEventListeners() {
    addRegisteredHandler(ConfirmationEvent.getType(), new ConfirmationEvent.Handler() {

      @Override
      public void onConfirmation(ConfirmationEvent event) {
        if(actionRequiringConfirmation != null && event.getSource().equals(actionRequiringConfirmation) &&
          event.isConfirmed()) {
          actionRequiringConfirmation.run();
          actionRequiringConfirmation = null;
        }
      }
    });

    addRegisteredHandler(VcfFileUploadRequestEvent.getType(), new VcfFileUploadRequestEvent.VcfFileUploadRequestHandler() {

      @Override
      public void onVcfFileUploadRequest(VcfFileUploadRequestEvent event) {
        uploadVcfFile(event.getFile(), event.getName());
      }
    });

    addRegisteredHandler(VcfMappingEditRequestEvent.getType(), new VcfMappingEditRequestEvent.VcfMappingEditRequestHandler() {
      @Override
      public void onVcfMappingEditRequest(VcfMappingEditRequestEvent event) {
        updateVCFMapping(event.getVCFSamplesMapping());
      }
    });

    addRegisteredHandler(VcfMappingDeleteRequestEvent.getType(), new VcfMappingDeleteRequestEvent.VcfMappingDeleteRequestHandler() {
      @Override
      public void onVcfMappingDeleteRequest(VcfMappingDeleteRequestEvent event) {
        removeVCFMapping(event.getProjectName());
      }

    });

    addRegisteredHandler(VcfFileExportRequestEvent.getType(), new VcfFileExportRequestEvent.VcfFileExportRequestHandler() {
      @Override
      public void onVcfFileExportRequest(VcfFileExportRequestEvent event) {
        exportVcfFiles(event.getCommand());
      }
    });
  }

  @Override
  protected void onBind() {
    super.onBind();
  }

  public void initialize(ProjectDto dto) {
    projectDto = dto;

    getView().clear(dto.hasVcfStoreService());
    if (dto.hasVcfStoreService()) {
      getParticipantTables();
      initializeParticipantIdentifierMappingList();
      authorize();
      refresh();
    }
  }

  private void initializeParticipantIdentifierMappingList() {
    Map<String, String> params = Maps.newHashMap();
    params.put(MAPPINGS_ENTITY_TYPE_PARAM, PARTICIPANT_ENTITY_TYPE);

    String uri = UriBuilders.IDENTIFIERS_MAPPINGS.create().query(params).build();
    ResourceRequestBuilderFactory.<JsArray<IdentifiersMappingDto>>newBuilder()
        .forResource(uri)
        .withCallback(new ResourceCallback<JsArray<IdentifiersMappingDto>>() {
          @Override
          public void onResource(Response response, JsArray<IdentifiersMappingDto> resource) {
            JsArray<IdentifiersMappingDto> identifiersMappings = JsArrays.toSafeArray(resource);
            participantIdentifiersMappingList.clear();
            for(IdentifiersMappingDto identifiersMappingDto : JsArrays.toIterable(identifiersMappings)) {
              participantIdentifiersMappingList.add(identifiersMappingDto.getName());
            }
          }
        })
        .get().send();
  }

  @Override
  public void onExportVcfFiles() {
    ProjectExportVcfFileModalPresenter modal = vcfFileDownloadModalPresenterModalProvider.get();
    boolean allSelected = getView().getAllVCFs().size() == getView().getSelectedVCFs().size()
        || getView().getSelectedVCFs().size() == 0;

    modal.setParticipantTables(participantTables);
    modal.setParticipantIdentifiersMappingList(participantIdentifiersMappingList);
    modal.showMappingDependantContent(mappingTable != null);
    modal.setExportVCFs(allSelected ? getView().getAllVCFs() : getView().getSelectedVCFs(), allSelected);
  }

  @Override
  public void onImportVcfFiles() {
    vcfFileUploadModalPresenterModalProvider.get();
  }

  @Override
  public void onEditMappingTable() {
    ProjectGenotypeEditMappingTableModalPresenter presenter = projectGenotypeEditMappingTableModalPresenterModalProvider.get();
    presenter.setVCFSamplesMapping(mappingTable, projectDto);
  }

  @Override
  public void onAddMappingTable() {
    onEditMappingTable();
  }

  @Override
  public void onDeleteMappingTable() {
    actionRequiringConfirmation = new Runnable() {
      @Override
      public void run() {
        fireEvent(new VcfMappingDeleteRequestEvent(projectDto.getName()));
      }
    };

    fireEvent(ConfirmationRequiredEvent
        .createWithMessages(actionRequiringConfirmation, translationMessages.removeVCFStoreMappingTable(),
            translationMessages.confirmDeleteVCFStoreMappingTable()));
  }

  @Override
  public void onRemoveVcfFile(Collection<VCFSummaryDto> vcfSummaryDto) {
    final Collection<VCFSummaryDto> vcfs = vcfSummaryDto;
    actionRequiringConfirmation = new Runnable() {
      @Override
      public void run() {
        removeVcfFile(vcfs);
      }
    };

    fireEvent(ConfirmationRequiredEvent
      .createWithMessages(actionRequiringConfirmation, translationMessages.removeVCFFile(),
        translationMessages.confirmDeleteVCFFile()));
  }

  @Override
  public void onDownloadStatistics(VCFSummaryDto vcfSummaryDto) {
    String downloadUrl = UriBuilders.PROJECT_VCF_STORE_VCF_EXPORT_STATS
      .create()
      .build(projectDto.getName(), vcfSummaryDto.getName());

    fireEvent(new FileDownloadRequestEvent(downloadUrl));
  }

  @Override
  public void onFilterUpdate(String filter) {
    if(Strings.isNullOrEmpty(filter)) {
      getView().renderRows(vcfSummaryDtos);
    } else {
      JsArray<VCFSummaryDto> filtered = JsArrays.create();
      for(VCFSummaryDto dto : JsArrays.toIterable(vcfSummaryDtos)) {
        if(vcfMatches(dto, filter)) {
          filtered.push(dto);
        }
      }
      getView().renderRows(filtered);
    }
  }

  @Override
  public void onMappingTableNavigateTo() {
    if (mappingTable != null) {
      String[] parts = mappingTable.getTableReference().split("\\.");
      PlaceRequest.Builder builder = new PlaceRequest.Builder().nameToken(Places.PROJECT)
        .with(ParameterTokens.TOKEN_NAME, parts[0]) //
        .with(ParameterTokens.TOKEN_TAB, ProjectPresenter.Display.ProjectTab.TABLES.toString()) //
        .with(ParameterTokens.TOKEN_PATH, mappingTable.getTableReference());

      placeManager.revealPlace(builder.build());
    }
  }

  @Override
  public void onMappingTableNavigateToVariable(String variable) {
    if (mappingTable != null) {
      String[] parts = mappingTable.getTableReference().split("\\.");
      PlaceRequest.Builder builder = new PlaceRequest.Builder().nameToken(Places.PROJECT)
        .with(ParameterTokens.TOKEN_NAME, parts[0]) //
        .with(ParameterTokens.TOKEN_TAB, ProjectPresenter.Display.ProjectTab.TABLES.toString()) //
        .with(ParameterTokens.TOKEN_PATH, mappingTable.getTableReference()+":"+variable);

      placeManager.revealPlace(builder.build());
    }
  }

  @Override
  public void onRefresh() {
    getVcfStore();
    getVcfSummaries();
  }

  private boolean vcfMatches(VCFSummaryDto dto, String filter) {
    String name = dto.getName().toLowerCase();
    return FilterHelper.matches(name, FilterHelper.tokenize(filter));
  }

  public void refresh() {
    onRefresh();
    getMappingTable();
  }

  private void authorize() {
    if (projectDto == null) return;
    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(UriBuilders.PROJECT_VCF_STORE_SAMPLES.create().build(projectDto.getName())) //
        .authorize(getView().getEditMappingAuthorizer()) //
        .put().send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(UriBuilders.PROJECT_VCF_STORE_IMPORT.create().build(projectDto.getName())) //
        .authorize(getView().getImportAuthorizer()) //
        .post().send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(UriBuilders.PROJECT_VCF_STORE_EXPORT.create().build(projectDto.getName())) //
        .authorize(getView().getExportAuthorizer()) //
        .post().send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(UriBuilders.PROJECT_VCF_STORE_VCFS.create().build(projectDto.getName())) //
        .authorize(getView().getRemoveVCF()) //
        .delete().send();

    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(
            UriBuilders.PROJECT_VCF_PERMISSIONS.create().build(projectDto.getName())) //
        .authorize(new CompositeAuthorizer(getView().getPermissionsAuthorizer(), new ProjectGenotypesPresenter.PermissionsUpdate())) //
        .post().send();
  }

  public void getVcfStore() {
    ResourceRequestBuilderFactory.<VCFStoreDto>newBuilder()
        .forResource(UriBuilders.PROJECT_VCF_STORE.create().build(projectDto.getName()))
        .withCallback(new ResourceCallback<VCFStoreDto>() {
          @Override
          public void onResource(Response response, VCFStoreDto vcfStoreDto) {
            getView().setVCFSamplesSummary(vcfStoreDto);
          }
        })
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().setVCFSamplesSummary(null);
          }
        }, Response.SC_FORBIDDEN)
        .get().send();
  }

  private void exportVcfFiles(ExportVCFCommandOptionsDto commandOptions) {
    commandOptions.setProject(projectDto.getName());

    ResourceRequestBuilderFactory
        .newBuilder()
        .withResourceBody(ExportVCFCommandOptionsDto.stringify(commandOptions))
        .forResource(UriBuilders.PROJECT_VCF_STORE_EXPORT.create().build(projectDto.getName()))
        .post()
        .withCallback(SC_CREATED, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            fireEvent(NotificationEvent.newBuilder().info("VCFFileExportTask").build());
            refresh();
          }
        })
        .send();
  }

  private void getVcfSummaries() {
    getView().beforeRenderRows();
    ResourceRequestBuilderFactory.<JsArray<VCFSummaryDto>>newBuilder()
        .forResource(UriBuilders.PROJECT_VCF_STORE_VCFS.create().build(projectDto.getName()))
        .withCallback(new ResourceCallback<JsArray<VCFSummaryDto>>() {
          @Override
          public void onResource(Response response, JsArray<VCFSummaryDto> summaries) {
            vcfSummaryDtos = summaries;
            getView().renderRows(summaries);
            getView().afterRenderRows();
          }
        })
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            if(response.getStatusCode() == Response.SC_FORBIDDEN) {
              fireEvent(NotificationEvent.newBuilder().warn("Forbidden").build());
            }
            vcfSummaryDtos = JsArrays.create();
            getView().renderRows(vcfSummaryDtos);
            getView().afterRenderRows();
          }
        }, Response.SC_FORBIDDEN)
        .get()
        .send();
  }

  private void getMappingTable() {
    ResourceRequestBuilderFactory.<VCFSamplesMappingDto>newBuilder()
        .forResource(UriBuilders.PROJECT_VCF_STORE_SAMPLES.create().build(projectDto.getName()))
        .withCallback(new ResourceCallback<VCFSamplesMappingDto>() {
          @Override
          public void onResource(Response response, VCFSamplesMappingDto vcfSamplesMappingDto) {
            mappingTable = vcfSamplesMappingDto;
            getView().setVCFSamplesMapping(vcfSamplesMappingDto);
          }
        })
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            mappingTable = null;
          }
        }, Response.SC_FORBIDDEN, Response.SC_NOT_FOUND)
        .get().send();
  }

  private void getParticipantTables() {
    Map<String, String> params = Maps.newHashMap();
    params.put(ENTITY_TYPE_PARAM, PARTICIPANT_ENTITY_TYPE);

    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder()
        .forResource(UriBuilders.DATASOURCES_TABLES.create().query(params).build())
        .withCallback(new ResourceCallback<JsArray<TableDto>>() {
          @Override
          public void onResource(Response response, JsArray<TableDto> resource) {
            participantTables = resource;
            logger.info(participantTables.length() + " PARTICIPANT tables");
          }
        }).get().send();
  }

  private void updateVCFMapping(VCFSamplesMappingDto dto) {
    ResourceRequestBuilderFactory
        .newBuilder()
        .forResource(UriBuilders.PROJECT_VCF_STORE_SAMPLES.create().build(projectDto.getName()))
        .withResourceBody(VCFSamplesMappingDto.stringify(dto))
        .put()
        .withCallback(SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            logger.info("Updated mapping table");
            refresh();
          }
        })
        .send();
  }

  private void removeVCFMapping(String projectName) {
    ResourceRequestBuilderFactory
        .newBuilder()
        .forResource(UriBuilders.PROJECT_VCF_STORE_SAMPLES.create().build(projectName))
        .delete()
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            fireEvent(ConfirmationTerminatedEvent.create());
            getView().clearMappingTable();
            mappingTable = null;
            refresh();
          }
        }, SC_NO_CONTENT)
        .send();
  }

  private void removeVcfFile(Collection<VCFSummaryDto> vcfSummaryDtos) {
    UriBuilder uri = UriBuilders.PROJECT_VCF_STORE_VCFS.create();
    for (VCFSummaryDto dto : vcfSummaryDtos) {
      HashMap<String, String> param = Maps.newHashMap();
      param.put(FILE_PARAM, dto.getName());
      uri.query(param);
    }

    ResourceRequestBuilderFactory
        .newBuilder()
        .forResource(uri.build(projectDto.getName()))
        .delete()
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            fireEvent(ConfirmationTerminatedEvent.create());
            refresh();
          }
        }, SC_NO_CONTENT)
        .send();
  }

  private void uploadVcfFile(String file, String name) {
    ImportVCFCommandOptionsDto importOptions = ImportVCFCommandOptionsDto.create();
    importOptions.setFilesArray(JsArrays.from(file));
    importOptions.setProject(projectDto.getName());

    ResourceRequestBuilderFactory
        .newBuilder()
        .withResourceBody(ImportVCFCommandOptionsDto.stringify(importOptions))
        .forResource(UriBuilders.PROJECT_VCF_STORE_IMPORT.create().build(projectDto.getName()))
        .post()
        .withCallback(SC_CREATED, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            fireEvent(NotificationEvent.newBuilder().info("VCFFileImportTask").build());
            refresh();
          }
        })
        .send();
  }

  private final class PermissionsUpdate implements HasAuthorization {

    @Override
    public void beforeAuthorization() {

    }

    @Override
    public void authorized() {
      ResourcePermissionsPresenter resourcePermissionsPresenter = resourcePermissionsProvider.get();
      resourcePermissionsPresenter.initialize(ResourcePermissionType.VCF_STORE,
          ResourcePermissionRequestPaths.UriBuilders.PROJECT_VCF_PERMISSIONS_STORE, projectDto.getName());

      setInSlot(TablePresenter.Display.Slots.Permissions, resourcePermissionsPresenter);
    }

    @Override
    public void unauthorized() {

    }
  }

  public interface Display extends View, HasUiHandlers<ProjectGenotypesUiHandlers> {

    List<VCFSummaryDto> getSelectedVCFs();

    List<VCFSummaryDto> getAllVCFs();

    void setVCFSamplesSummary(VCFStoreDto dto);

    void setVCFSamplesMapping(VCFSamplesMappingDto dto);

    void beforeRenderRows();

    void renderRows(JsArray<VCFSummaryDto> rows);

    void afterRenderRows();

    void clear(boolean hasVcfService);

    void clearMappingTable();

    HasAuthorization getPermissionsAuthorizer();

    HasAuthorization getImportAuthorizer();

    HasAuthorization getExportAuthorizer();

    HasAuthorization getEditMappingAuthorizer();

    HasAuthorization getRemoveVCF();
  }
}
