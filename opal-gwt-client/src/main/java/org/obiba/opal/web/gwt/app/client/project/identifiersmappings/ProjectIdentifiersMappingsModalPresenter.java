/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.identifiersmappings;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.project.identifiersmappings.event.IdentifiersMappingAddedEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.opal.ProjectDto;

import javax.inject.Inject;
import java.util.List;

public class ProjectIdentifiersMappingsModalPresenter extends ModalPresenterWidget<ProjectIdentifiersMappingsModalPresenter.Display>
  implements ProjectIdentifiersMappingsModalUiHandlers {

  private final Translations translations;
  private List<TableDto> mappingTables;

  @Inject
  public ProjectIdentifiersMappingsModalPresenter(EventBus eventBus, Display view, Translations translations) {
    super(eventBus, view);
    getView().setUiHandlers(this);
    this.translations = translations;
  }

  public void initialize(List<TableDto> mappingTables, ProjectDto.IdentifiersMappingDto mapping) {
    getView().clearEntityTypes();
    this.mappingTables = mappingTables;
    for (TableDto table : mappingTables) {
      getView().addEntityType(table);
    }

    if (mapping == null) {
      getMappings(mappingTables.get(0).getName(), null);
    } else {
      getView().selectEntityType(mapping.getName());
      getMappings(mapping.getName(), mapping.getMapping());
    }
  }

  private void getMappings(final String tableName, final String variableName) {
    String uri = UriBuilders.IDENTIFIERS_TABLE_VARIABLES.create().build(tableName);
    ResourceRequestBuilderFactory.<JsArray<VariableDto>>newBuilder() //
      .forResource(uri) //
      .withCallback(new ResourceCallback<JsArray<VariableDto>>() {
        @Override
        public void onResource(Response response, JsArray<VariableDto> dtos) {
          getView().clearMappings();
          List<VariableDto> variables = JsArrays.toList(dtos);
          for (VariableDto variable : variables) {
            getView().addMapping(variable.getName());
          }

          if (!Strings.isNullOrEmpty(variableName)) {
            getView().selectMapping(variableName);
          }
        }
      }) //
      .get().send();
  }

  @Override
  public void updateMappings(String tableName) {
    getMappings(tableName, null);
  }

  @Override
  public void save(ProjectDto.IdentifiersMappingDto mapping) {
    getEventBus().fireEvent(new IdentifiersMappingAddedEvent(mapping));
    getView().hideDialog();
  }

  public interface Display extends PopupView, HasUiHandlers<ProjectIdentifiersMappingsModalUiHandlers> {
    void hideDialog();

    void initialize(ProjectDto.IdentifiersMappingDto identifiersMappingDto);

    void clearEntityTypes();

    void clearMappings();

    void addEntityType(TableDto type);

    void addMapping(String name);

    void selectEntityType(String entityType);

    void selectMapping(String variableName);
  }
}
