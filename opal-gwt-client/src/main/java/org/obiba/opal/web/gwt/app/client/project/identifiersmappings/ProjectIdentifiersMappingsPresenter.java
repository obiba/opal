package org.obiba.opal.web.gwt.app.client.project.identifiersmappings;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.project.event.ProjectUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.project.identifiersmappings.event.IdentifiersMappingAddedEvent;
import org.obiba.opal.web.gwt.app.client.support.ErrorResponseCallback;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.ProjectDto;
import org.obiba.opal.web.model.client.opal.ProjectFactoryDto;

import javax.inject.Inject;
import java.util.List;

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_OK;

public class ProjectIdentifiersMappingsPresenter extends PresenterWidget<ProjectIdentifiersMappingsPresenter.Display>
  implements ProjectIdentifiersMappingsUiHandlers {

  private final ModalProvider<ProjectIdentifiersMappingsModalPresenter> modalProvider;

  private ProjectDto project;
  private List<TableDto> mappingTables;

  @Inject
  public ProjectIdentifiersMappingsPresenter(EventBus eventBus,
                                             Display view,
                                             ModalProvider<ProjectIdentifiersMappingsModalPresenter> modalProvider) {
    super(eventBus, view);
    this.modalProvider = modalProvider.setContainer(this);
    getView().setUiHandlers(this);
    initializeEventListeners();
  }

  public void setProject(ProjectDto project) {
    this.project = project;
    getView().setIdentifiersMappings(JsArrays.toList(this.project.getIdMappingsArray()));
    initializeMappingTables();
  }

  @Override
  public void addIdMappings() {
    ProjectIdentifiersMappingsModalPresenter modal = modalProvider.get();
    modal.initialize(mappingTables);
  }

  private void initializeMappingTables() {
    String uri = UriBuilders.IDENTIFIERS_TABLES.create().query("counts", "true").build();
    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder() //
      .forResource(uri) //
      .withCallback(new ResourceCallback<JsArray<TableDto>>() {

        @Override
        public void onResource(Response response, JsArray<TableDto> dtos) {
          ensureValidMappingTables(JsArrays.toList(dtos));
        }
      }) //
      .get().send();
  }

  private void ensureValidMappingTables(List<TableDto> tables) {
    mappingTables = Lists.newArrayList();

    if (tables.size() > 0) {
      for (TableDto table : tables) {
        if (table.getVariableCount() > 0) {
          mappingTables.add(table);
        }
      }
    }

    getView().enableView(mappingTables.size() > 0);
  }

  private void initializeEventListeners() {
    getView().getActionColumn().setActionHandler(new ActionHandler<ProjectDto.IdentifiersMappingDto>() {
      @Override
      public void doAction(ProjectDto.IdentifiersMappingDto dto, String actionName) {
        switch (actionName) {
          case Display.ADD_MAPPING:
            break;
          case Display.EDIT_MAPPING:
            break;
          case Display.DELETE_MAPPING:
            break;
        }
      }
    });

    addRegisteredHandler(IdentifiersMappingAddedEvent.getType(), new IdentifiersMappingAddedEvent.IdentifiersMappingAddedHandler() {
      @Override
      public void onIdentifiersMappingAdded(IdentifiersMappingAddedEvent event) {
        updateProjectIdMappings(event.getAnalysisDto());
      }
    });
  }

  private void updateProjectIdMappings(ProjectDto.IdentifiersMappingDto newMapping) {
    JsArray<ProjectDto.IdentifiersMappingDto> idMappings = project.getIdMappingsArray();
    if (idMappings == null) {
      idMappings = JsArrays.create();
      project.setIdMappingsArray(idMappings);
    }

    int foundIndex = -1;

    for (int i = 0, length = idMappings.length(); i < length && foundIndex <= -1; i++) {
      ProjectDto.IdentifiersMappingDto idMapping = idMappings.get(i);
      if (idMapping.getEntityType().equals(newMapping.getEntityType())) {
        foundIndex = i;
      }
    }

    if (foundIndex > -1) {
      idMappings.set(foundIndex, newMapping);
    } else {
      idMappings.push(newMapping);
    }

    getView().setIdentifiersMappings(JsArrays.toList(idMappings));
    update();
  }

  private void update() {
    ResourceRequestBuilderFactory.<ProjectFactoryDto>newBuilder() //
      .forResource(UriBuilders.PROJECT.create().build(project.getName()))  //
      .withResourceBody(ProjectDto.stringify(project)) //
      .withCallback(SC_OK, new ResponseCodeCallback() {
        @Override
        public void onResponseCode(Request request, Response response) {
          fireEvent(new ProjectUpdatedEvent(project));
        }
      }) //
      .withCallback(SC_BAD_REQUEST, new ErrorResponseCallback(getView().asWidget()) {
        @Override
        public void onResponseCode(Request request, Response response) {
          super.onResponseCode(request, response);
        }
      }) //
      .put().send();
  }

  public interface Display extends View, HasUiHandlers<ProjectIdentifiersMappingsUiHandlers> {
    String ADD_MAPPING = "addMapping";
    String EDIT_MAPPING = "editMapping";
    String DELETE_MAPPING = "deleteMapping";

    HasActionHandler<ProjectDto.IdentifiersMappingDto> getActionColumn();

    void setIdentifiersMappings(List<ProjectDto.IdentifiersMappingDto> idMappingsArray);

    void enableView(boolean b);
  }
}
