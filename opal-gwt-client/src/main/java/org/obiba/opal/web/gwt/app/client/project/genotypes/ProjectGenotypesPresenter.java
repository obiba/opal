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
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.project.genotypes.event.VcfFileUploadRequestEvent;
import org.obiba.opal.web.gwt.app.client.project.genotypes.event.VcfMappingEditRequestEvent;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.ProjectDto;
import org.obiba.opal.web.model.client.opal.VCFSamplesMappingDto;
import org.obiba.opal.web.model.client.opal.VCFSamplesSummaryDto;
import org.obiba.opal.web.model.client.opal.VCFSummaryDto;

import java.util.Map;
import java.util.logging.Logger;

import static com.google.gwt.http.client.Response.SC_NO_CONTENT;
import static com.google.gwt.http.client.Response.SC_OK;

public class ProjectGenotypesPresenter extends PresenterWidget<ProjectGenotypesPresenter.Display>
    implements ProjectGenotypesUiHandlers {

  private static final String ENTITY_TYPE_PARAM = "entityType";

  private static final String ENTITY_TYPE = "Sample";

  private static Logger logger = Logger.getLogger("ProjectGenotypesPresenter");

  private ProjectDto projectDto;

  private final ModalProvider<ProjectImportVcfFileModalPresenter> vcfFileUploadModalPresenterModalProvider;

  private final ModalProvider<ProjectExportVcfFileModalPresenter> vcfFileDownloadModalPresenterModalProvider;

  private final ModalProvider<ProjectGenotypeEditMappingTableModalPresenter> projectGenotypeEditMappingTableModalPresenterModalProvider;

  private JsArray<TableDto> mappingTables = JsArrays.create();

  @Inject
  public ProjectGenotypesPresenter(Display display, EventBus eventBus,
                                   ModalProvider<ProjectImportVcfFileModalPresenter> vcfFileUploadModalPresenterModalProvider,
                                   ModalProvider<ProjectExportVcfFileModalPresenter> vcfFileDownloadModalPresenterModalProvider,
                                   ModalProvider<ProjectGenotypeEditMappingTableModalPresenter> editMappingTableModalPresenterModalProvider) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    this.vcfFileUploadModalPresenterModalProvider = vcfFileUploadModalPresenterModalProvider.setContainer(this);
    this.vcfFileDownloadModalPresenterModalProvider = vcfFileDownloadModalPresenterModalProvider.setContainer(this);
    projectGenotypeEditMappingTableModalPresenterModalProvider = editMappingTableModalPresenterModalProvider.setContainer(this);
    initializeEventListeners();
  }

  private void initializeEventListeners() {
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
  public void onDownloadVcfFiles() {
    vcfFileDownloadModalPresenterModalProvider.get();
  }

  @Override
  public void onImportVcfFiles() {
    vcfFileUploadModalPresenterModalProvider.get();
  }

  @Override
  public void onEditMappingTable() {
    ProjectGenotypeEditMappingTableModalPresenter presenter = projectGenotypeEditMappingTableModalPresenterModalProvider.create();
    presenter.setMappingTables(mappingTables);
    presenter.setVCFSamplesMapping(currentGenotypesMapping());
    projectGenotypeEditMappingTableModalPresenterModalProvider.show();
  }

  @Override
  public void onRemoveVcfFile(VCFSummaryDto vcfSummaryDto) {
    removeVcfFile(vcfSummaryDto.getName());
  }

  @Override
  public void onDownloadVcfFile(VCFSummaryDto vcfSummaryDto) {

  }

  @Override
  public void onDownloadStatistics(VCFSummaryDto vcfSummaryDto) {

  }

  public void refresh() {
    getGenotypesSummary();
    getMappingTable();
    getVcfTables();
  }

  public void getGenotypesSummary() {
    ResourceRequestBuilderFactory.<VCFSamplesSummaryDto>newBuilder()
      .forResource(UriBuilders.PROJECT_GENOTYPES_SUMMARY.create().build(projectDto.getName()))
      .withCallback(new ResourceCallback<VCFSamplesSummaryDto>() {
        @Override
        public void onResource(Response response, VCFSamplesSummaryDto summary) {
          logger.info("Received Genotypes summary");
          getView().setVCFSamplesSummary(summary);
        }
      }).get().send();
  }

  private void getVcfTables() {
    getView().beforeRenderRows();
    ResourceRequestBuilderFactory.<JsArray<VCFSummaryDto>>newBuilder()
      .forResource(UriBuilders.PROJECT_VCF_STORE_VCFS.create().build(projectDto.getName()))
      .withCallback(new ResourceCallback<JsArray<VCFSummaryDto>>() {
        @Override
        public void onResource(Response response, JsArray<VCFSummaryDto> summaries) {
          getView().renderRows(summaries);
          getView().afterRenderRows();
        }
      })
      .get()
      .send();
  }

  private void getMappingTable() {
    getView().setVCFSamplesMapping(currentGenotypesMapping());
    Map<String, String> params = Maps.newHashMap();
    params.put(ENTITY_TYPE_PARAM, ENTITY_TYPE);

    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder()
        .forResource(UriBuilders.DATASOURCE_TABLES.create().query(params).build(projectDto.getName()))
        .withCallback(new ResourceCallback<JsArray<TableDto>>() {
          @Override
          public void onResource(Response response, JsArray<TableDto> resource) {
            mappingTables = resource;
            logger.info(mappingTables.length() + " mapping tables");
          }
        }).get().send();
  }

  private VCFSamplesMappingDto currentGenotypesMapping() {
    VCFSamplesMappingDto vcfSamplesMappingDto =
      projectDto.hasVcfSamplesMapping() ? projectDto.getVcfSamplesMapping() : VCFSamplesMappingDto.create();
    vcfSamplesMappingDto.setProjectName(projectDto.getName());
    return vcfSamplesMappingDto;
  }

  private void updateVCFMapping(VCFSamplesMappingDto dto) {
    projectDto.setVcfSamplesMapping(dto);

    ResourceRequestBuilderFactory
        .newBuilder()
        .forResource(UriBuilders.PROJECT.create().build(projectDto.getName()))
        .withResourceBody(ProjectDto.stringify(projectDto))
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

  private void removeVcfFile(String name) {
    logger.info("Remove VCF file " + name);
    ResourceRequestBuilderFactory
        .newBuilder()
        .forResource(UriBuilders.PROJECT_VCF_STORE_VCF.create().build(projectDto.getName(), name))
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
    logger.info("Upload requested " + file + " " + name);
    UriBuilder uri = UriBuilders.PROJECT_VCF_STORE_VCFS
        .create()
        .query("file", file);

    if (!Strings.isNullOrEmpty(name)) uri.query("name", name);

    ResourceRequestBuilderFactory
        .newBuilder()
        .forResource(uri.build(projectDto.getName()))
        .post()
        .withCallback(SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            refresh();
          }
        })
        .send();
  }

  public interface Display extends View, HasUiHandlers<ProjectGenotypesUiHandlers> {

    void setVCFSamplesSummary(VCFSamplesSummaryDto dto);

    void setVCFSamplesMapping(VCFSamplesMappingDto dto);

    void beforeRenderRows();

    void renderRows(JsArray<VCFSummaryDto> rows);

    void afterRenderRows();
  }
}
