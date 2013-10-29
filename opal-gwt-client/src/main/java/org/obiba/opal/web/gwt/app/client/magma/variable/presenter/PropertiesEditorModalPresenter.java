/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.variable.presenter;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.magma.event.VariableRefreshEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

/**
 *
 */
public class PropertiesEditorModalPresenter extends ModalPresenterWidget<PropertiesEditorModalPresenter.Display>
    implements PropertiesEditorModalUiHandlers {

  private static final Translations translations = GWT.create(Translations.class);

  private VariableDto variable;

  private TableDto tableDto;

  @Inject
  public PropertiesEditorModalPresenter(EventBus eventBus, Display display) {
    super(eventBus, display);
  }

  public void initialize(VariableDto dto, TableDto table) {
    variable = dto;
    tableDto = table;

    getView().setUiHandlers(this);
    getView().renderProperties(dto);
  }

  @Override
  public void onSave() {
    VariableDto v = getVariableDto();

    // If variable from a view
    if(Strings.isNullOrEmpty(tableDto.getViewLink())) {
      ResourceRequestBuilderFactory.newBuilder().forResource(UriBuilders.DATASOURCE_TABLE_VARIABLE.create()
          .build(tableDto.getDatasourceName(), tableDto.getName(), v.getName())) //
          .put() //
          .withResourceBody(VariableDto.stringify(v)).accept("application/json") //
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              if(response.getStatusCode() != Response.SC_OK) {
                getView().showError(response.getText(), null);
              } else {
                getView().hide();
              }
              fireEvent(new VariableRefreshEvent());
            }
          }, Response.SC_BAD_REQUEST, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_OK).send();
    } else {
      UriBuilder uriBuilder = UriBuilder.create().segment("datasource", "{}", "view", "{}", "variable", "{}")
          .query("comment",
              TranslationsUtils.replaceArguments(translations.updateVariableProperties(), variable.getName()));

      ResourceRequestBuilderFactory.newBuilder()
          .forResource(uriBuilder.build(tableDto.getDatasourceName(), tableDto.getName(), variable.getName())) //
          .put() //
          .withResourceBody(VariableDto.stringify(v)).accept("application/json") //
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              if(response.getStatusCode() == Response.SC_OK) {
                getView().hide();
              } else {
                getView().showError(response.getText(), null);
              }
              fireEvent(new VariableRefreshEvent());
            }
          }, Response.SC_BAD_REQUEST, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_OK).send();
    }
  }

  private VariableDto getVariableDto() {
    VariableDto v = VariableDto.create();
    v.setLink(variable.getLink());
    v.setIndex(variable.getIndex());
    v.setIsNewVariable(variable.getIsNewVariable());
    v.setParentLink(variable.getParentLink());
    v.setName(variable.getName());
    v.setEntityType(variable.getEntityType());
    v.setValueType(variable.getValueType());

    if(variable.getAttributesArray().length() > 0) {
      v.setAttributesArray(variable.getAttributesArray());
    }

    if(variable.getCategoriesArray().length() > 0) {
      v.setCategoriesArray(variable.getCategoriesArray());
    }

    // Update info from view
    v.setUnit(getView().getUnit());
    v.setIsRepeatable(getView().getRepeatable());
    v.setReferencedEntityType(getView().getReferencedEntityType());
    v.setMimeType(getView().getMimeType());
    v.setOccurrenceGroup(getView().getOccurenceGroup());
    return v;
  }

  public interface Display extends PopupView, HasUiHandlers<PropertiesEditorModalUiHandlers> {
    void renderProperties(VariableDto variable);

    void showError(String message, @Nullable ControlGroup group);

    boolean getRepeatable();

    String getUnit();

    String getReferencedEntityType();

    String getMimeType();

    String getOccurenceGroup();

  }
}
