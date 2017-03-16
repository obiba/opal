/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
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
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.project.genotypes.event.VcfFileExportRequestEvent;
import org.obiba.opal.web.gwt.app.client.project.genotypes.event.VcfFileUploadRequestEvent;
import org.obiba.opal.web.gwt.app.client.project.genotypes.event.VcfMappingEditRequestEvent;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.google.gwt.http.client.Response.*;

public class ProjectGenotypesPresenter extends PresenterWidget<ProjectGenotypesPresenter.Display>
    implements ProjectGenotypesUiHandlers {

  private static final String ENTITY_TYPE_PARAM = "entityType";

  private static final String FILE_PARAM = "file";

  private static final String ENTITY_TYPE = "Sample";

  private static Logger logger = Logger.getLogger("ProjectGenotypesPresenter");

  private ProjectDto projectDto;

  private final TranslationMessages translationMessages;

  private final ModalProvider<ProjectImportVcfFileModalPresenter> vcfFileUploadModalPresenterModalProvider;

  private final ModalProvider<ProjectExportVcfFileModalPresenter> vcfFileDownloadModalPresenterModalProvider;

  private final ModalProvider<ProjectGenotypeEditMappingTableModalPresenter> projectGenotypeEditMappingTableModalPresenterModalProvider;

  private JsArray<TableDto> mappingTables = JsArrays.create();

  private VCFSamplesMappingDto mappingTable = VCFSamplesMappingDto.create();

  private JsArray<VCFSummaryDto> vcfSummaryDtos = JsArrays.create();

  private Runnable actionRequiringConfirmation;

  @Inject
  public ProjectGenotypesPresenter(Display display, EventBus eventBus,
                                   ModalProvider<ProjectImportVcfFileModalPresenter> vcfFileUploadModalPresenterModalProvider,
                                   ModalProvider<ProjectExportVcfFileModalPresenter> vcfFileDownloadModalPresenterModalProvider,
                                   ModalProvider<ProjectGenotypeEditMappingTableModalPresenter> editMappingTableModalPresenterModalProvider,
                                   TranslationMessages translationMessages) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    this.translationMessages = translationMessages;
    this.vcfFileUploadModalPresenterModalProvider = vcfFileUploadModalPresenterModalProvider.setContainer(this);
    this.vcfFileDownloadModalPresenterModalProvider = vcfFileDownloadModalPresenterModalProvider.setContainer(this);
    projectGenotypeEditMappingTableModalPresenterModalProvider = editMappingTableModalPresenterModalProvider.setContainer(this);
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
    refresh();
  }

  @Override
  public void onExportVcfFiles() {
    ProjectExportVcfFileModalPresenter provider = vcfFileDownloadModalPresenterModalProvider.get();
    boolean allSelected = getView().getAllVCFs().size() == getView().getSelectedVCFs().size()
        || getView().getSelectedVCFs().size() == 0;

    provider.setExportVCFs(allSelected ? getView().getAllVCFs() : getView().getSelectedVCFs(), allSelected);
  }

  @Override
  public void onImportVcfFiles() {
    vcfFileUploadModalPresenterModalProvider.get();
  }

  @Override
  public void onEditMappingTable() {
    ProjectGenotypeEditMappingTableModalPresenter presenter = projectGenotypeEditMappingTableModalPresenterModalProvider.get();
    presenter.setMappingTables(mappingTables);
    presenter.setVCFSamplesMapping(mappingTable, projectDto);
  }

  @Override
  public void onRemoveAll() {
    actionRequiringConfirmation = new Runnable() {
      @Override
      public void run() {
        removeAll();
      }
    };

    fireEvent(ConfirmationRequiredEvent
      .createWithMessages(actionRequiringConfirmation, translationMessages.removeAllGenotypesData(),
        translationMessages.confirmRemoveAllGenotypesData()));
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

  private boolean vcfMatches(VCFSummaryDto dto, String filter) {
    String name = dto.getName().toLowerCase();
    for(String token : filter.toLowerCase().split(" ")) {
      if(!Strings.isNullOrEmpty(token)) {
        if(!name.contains(token))
          return false;
      }
    }
    return true;
  }

  public void refresh() {
    getVcfStore();
    getMappingTable();
    getMappingTables();
    getVcfSummaries();
  }

  public void getVcfStore() {
    ResourceRequestBuilderFactory.<VCFStoreDto>newBuilder()
      .forResource(UriBuilders.PROJECT_VCF_STORE.create().build(projectDto.getName()))
      .withCallback(new ResourceCallback<VCFStoreDto>() {
        @Override
        public void onResource(Response response, VCFStoreDto vcfStoreDto) {
          getView().setVCFSamplesSummary(vcfStoreDto);
        }
      }).get().send();
  }

  private void exportVcfFiles(ExportVCFCommandOptionsDto commandOptions) {
    commandOptions.setProject(projectDto.getName());
    commandOptions.setTable(mappingTable.getTableReference());

    ResourceRequestBuilderFactory
        .newBuilder()
        .withResourceBody(ExportVCFCommandOptionsDto.stringify(commandOptions))
        .forResource(UriBuilders.PROJECT_VCF_STORE_EXPORT.create().build(projectDto.getName()))
        .post()
        .withCallback(SC_CREATED, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
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
      .withCallback(SC_NOT_FOUND, new ResponseCodeCallback() {
        @Override
        public void onResponseCode(Request request, Response response) {
          mappingTable = VCFSamplesMappingDto.create();
          getView().clearSamplesMappingData();
        }
      })
      .get().send();
  }

  private void getMappingTables() {
    Map<String, String> params = Maps.newHashMap();
    params.put(ENTITY_TYPE_PARAM, ENTITY_TYPE);

    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder()
        .forResource(UriBuilders.DATASOURCES_TABLES.create().query(params).build())
        .withCallback(new ResourceCallback<JsArray<TableDto>>() {
          @Override
          public void onResource(Response response, JsArray<TableDto> resource) {
            mappingTables = resource;
            logger.info(mappingTables.length() + " mapping tables");
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

  private void removeAll() {
    ResourceRequestBuilderFactory
      .newBuilder()
      .forResource(UriBuilders.PROJECT_VCF_STORE.create().build(projectDto.getName()))
      .delete()
      .withCallback(new ResponseCodeCallback() {
        @Override
        public void onResponseCode(Request request, Response response) {
          logger.info("Deleted ALL");
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
            logger.info("Deleted file");
            refresh();
          }
        }, SC_NO_CONTENT)
        .send();
  }

  private void uploadVcfFile(String file, String name) {
    ImportVCFCommandOptionsDto importOptions = ImportVCFCommandOptionsDto.create();
    importOptions.setFile(file);
    importOptions.setProject(projectDto.getName());
    if (!Strings.isNullOrEmpty(name)) importOptions.setName(name);

    ResourceRequestBuilderFactory
        .newBuilder()
        .withResourceBody(ImportVCFCommandOptionsDto.stringify(importOptions))
        .forResource(UriBuilders.PROJECT_VCF_STORE_IMPORT.create().build(projectDto.getName()))
        .post()
        .withCallback(SC_CREATED, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            refresh();
          }
        })
        .send();
  }

  public interface Display extends View, HasUiHandlers<ProjectGenotypesUiHandlers> {

    List<VCFSummaryDto> getSelectedVCFs();

    List<VCFSummaryDto> getAllVCFs();

    void setVCFSamplesSummary(VCFStoreDto dto);

    void setVCFSamplesMapping(VCFSamplesMappingDto dto);

    void beforeRenderRows();

    void renderRows(JsArray<VCFSummaryDto> rows);

    void afterRenderRows();

    void clearSamplesMappingData();
  }
}
