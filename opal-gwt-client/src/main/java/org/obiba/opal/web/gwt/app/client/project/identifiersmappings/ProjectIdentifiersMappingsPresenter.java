/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.identifiersmappings;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import org.obiba.opal.web.gwt.app.client.js.JsArrayIterator;
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
import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.EDIT_ACTION;
import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.REMOVE_ACTION;

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
    initializeIdentifiersMappings();
  }

  @Override
  public void addIdMappings() {
    initializeMappingTables(new IdentifiersTablesResourceSuccessCallback() {
      @Override
      public void onSuccess() {
        ProjectIdentifiersMappingsModalPresenter modal = modalProvider.get();
        modal.initialize(mappingTables, null);
      }
    });
  }

  @Override
  public void editIdMapping(final ProjectDto.IdentifiersMappingDto mapping) {
    initializeMappingTables(new IdentifiersTablesResourceSuccessCallback() {
      @Override
      public void onSuccess() {
        ProjectIdentifiersMappingsModalPresenter modal = modalProvider.get();
        modal.initialize(mappingTables, mapping);
      }
    });
  }

  @Override
  public void removeIdMapping(ProjectDto.IdentifiersMappingDto mapping) {
    removeMappingFromProject(mapping);
    update();
  }

  private void removeMappingFromProject(ProjectDto.IdentifiersMappingDto mapping) {
    JsArray<ProjectDto.IdentifiersMappingDto> idMappings = JsArrays.create();
    JsArrayIterator iterator = new JsArrayIterator(project.getIdMappingsArray());

    while(iterator.hasNext()) {
      ProjectDto.IdentifiersMappingDto target = (ProjectDto.IdentifiersMappingDto)iterator.next();
      if (!target.getEntityType().equals(mapping.getEntityType())) {
        idMappings.push(target);
      }
    }

    project.setIdMappingsArray(idMappings);
  }

  private void initializeIdentifiersMappings() {
    String uri = UriBuilders.PROJECT_IDENTIFIERS_MAPPINGS.create().build(project.getName());
    ResourceRequestBuilderFactory.<JsArray<ProjectDto.IdentifiersMappingDto>>newBuilder() //
      .forResource(uri) //
      .withCallback(new ResourceCallback<JsArray<ProjectDto.IdentifiersMappingDto>>() {

        @Override
        public void onResource(Response response, JsArray<ProjectDto.IdentifiersMappingDto> mappings) {
          getView().setIdentifiersMappings(JsArrays.toList(mappings));
          initializeMappingTables(null);
        }
      }) //
      .get().send();
  }

  private void initializeMappingTables(final IdentifiersTablesResourceSuccessCallback successCallback) {
    String uri = UriBuilders.IDENTIFIERS_TABLES.create().query("counts", "true").build();
    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder() //
      .forResource(uri) //
      .withCallback(new ResourceCallback<JsArray<TableDto>>() {

        @Override
        public void onResource(Response response, JsArray<TableDto> dtos) {
          ensureValidMappingTables(JsArrays.toList(dtos));
          if (successCallback != null) successCallback.onSuccess();
        }
      })
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            ensureValidMappingTables(null);
          }
        }, Response.SC_NOT_FOUND, Response.SC_FORBIDDEN)
      .get().send();
  }

  private void ensureValidMappingTables(List<TableDto> tables) {
    mappingTables = Lists.newArrayList();

    if (tables != null && tables.size() > 0) {
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
          case EDIT_ACTION:
            editIdMapping(dto);
            break;
          case REMOVE_ACTION:
            removeIdMapping(dto);
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

  interface IdentifiersTablesResourceSuccessCallback {
    void onSuccess();
  }

  public interface Display extends View, HasUiHandlers<ProjectIdentifiersMappingsUiHandlers> {

    HasActionHandler<ProjectDto.IdentifiersMappingDto> getActionColumn();

    void setIdentifiersMappings(List<ProjectDto.IdentifiersMappingDto> idMappingsArray);

    void enableView(boolean b);
  }
}
