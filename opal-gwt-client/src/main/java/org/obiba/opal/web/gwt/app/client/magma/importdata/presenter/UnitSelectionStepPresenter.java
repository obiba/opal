/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.importdata.presenter;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.IdentifiersMappingDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class UnitSelectionStepPresenter extends PresenterWidget<UnitSelectionStepPresenter.Display> {

  @Inject
  public UnitSelectionStepPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    super.onBind();
    addEventHandlers();
    initIdentifiersMappings();
  }

  private void addEventHandlers() {
    registerHandler(getView().addIdentifierAsIsClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getView().setIdentifiersMappingEnabled(false);
      }
    }));
    registerHandler(getView().addIdentifierSharedWithUnitClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getView().setIdentifiersMappingEnabled(true);
      }
    }));
  }

  private void initIdentifiersMappings() {

    ResponseCodeCallback errorCallback = new ResponseCodeCallback() {
      @Override
      public void onResponseCode(Request request, Response response) {
      }
    };

    ResourceRequestBuilderFactory.<JsArray<IdentifiersMappingDto>>newBuilder().forResource("/identifiers/mappings")
        .get().withCallback(new ResourceCallback<JsArray<IdentifiersMappingDto>>() {
      @Override
      public void onResource(Response response, JsArray<IdentifiersMappingDto> resource) {
        getView().setIdentifiersMappings(JsArrays.toSafeArray(resource));
      }

    }).withCallback(Response.SC_FORBIDDEN, errorCallback).send();
  }

  public void setEntityType(String entityType) {
    getView().setUnitRadiosEnabled(Strings.isNullOrEmpty(entityType) || "Participant".equalsIgnoreCase(entityType));
  }

  public void updateImportConfig(ImportConfig importConfig) {
    boolean withUnit = getView().isIdentifierSharedWithUnit();
    importConfig.setIdentifierSharedWithUnit(withUnit);
    importConfig.setIdentifierAsIs(!withUnit);
    importConfig.setIdentifiersMapping(withUnit ? getView().getSelectedIdentifiersMapping() : null);
    importConfig.setIncremental(getView().isIncremental());
    importConfig.setLimit(getView().getLimit());
  }

  public interface Display extends View {

    boolean isIdentifierSharedWithUnit();

    void setIdentifiersMappings(JsArray<IdentifiersMappingDto> mappings);

    String getSelectedIdentifiersMapping();

    HandlerRegistration addIdentifierAsIsClickHandler(ClickHandler handler);

    HandlerRegistration addIdentifierSharedWithUnitClickHandler(ClickHandler handler);

    void setIdentifiersMappingEnabled(boolean enabled);

    boolean isIncremental();

    Integer getLimit();

    void setUnitRadiosEnabled(boolean enabled);
  }

}
