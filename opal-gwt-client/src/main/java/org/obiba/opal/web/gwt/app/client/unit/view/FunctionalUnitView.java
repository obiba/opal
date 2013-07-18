/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.unit.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.support.BreadcrumbsBuilder;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitPresenter;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitsUiHandlers;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.UIObjectAuthorizer;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public class FunctionalUnitView extends ViewWithUiHandlers<FunctionalUnitsUiHandlers> implements FunctionalUnitPresenter.Display {

  interface Binder extends UiBinder<Widget, FunctionalUnitView> {}

  @UiField
  Button functionalUnitButton;

  @UiField
  Button exportButton;

  @UiField
  Button importButton;

  @UiField
  Button syncButton;

  @UiField
  ScrollPanel functionalUnitDetailsPanel;

  @UiField
  ScrollPanel functionalUnitListPanel;

  @UiField
  Panel content;

  @UiField
  Panel breadcrumbs;

  private final PlaceManager placeManager;

  @Inject
  public FunctionalUnitView(Binder binder, PlaceManager placeManager) {
    initWidget(binder.createAndBindUi(this));
    this.placeManager = placeManager;
  }

  @Override
  public void setFunctionalUnits(JsArray<FunctionalUnitDto> templates) {
    content.clear();

    for(FunctionalUnitDto unit : JsArrays.toIterable(templates)) {
      FlowPanel panel = new FlowPanel();
      panel.addStyleName("item");
      PlaceRequest.Builder requestBuilder = new PlaceRequest.Builder();
      requestBuilder.nameToken(Places.unit).with("name", unit.getName());
      Hyperlink unitLink = new Hyperlink(unit.getName(), placeManager.buildRelativeHistoryToken(requestBuilder.build()));
      panel.add(unitLink);
      Label descriptionLabel = new Label(unit.getDescription());
      panel.add(descriptionLabel);
      FlowPanel tagsPanel = new FlowPanel();
      tagsPanel.addStyleName("tags");
      panel.add(tagsPanel);
      content.add(panel);
    }
  }

  @Override
  public HasAuthorization getAddFunctionalUnitAuthorizer() {
    return new UIObjectAuthorizer(functionalUnitButton);
  }

  @Override
  public HasAuthorization getExportIdentifiersAuthorizer() {
    return new UIObjectAuthorizer(exportButton);
  }

  @Override
  public HasAuthorization getImportIdentifiersAuthorizer() {
    return new UIObjectAuthorizer(importButton);
  }

  @Override
  public HasAuthorization getSyncIdentifiersAuthorizer() {
    return new UIObjectAuthorizer(syncButton);
  }

  @Override
  public void setBreadcrumbItems(List<BreadcrumbsBuilder.Item> items) {
    breadcrumbs.add(new BreadcrumbsBuilder().setItems(items).build());
  }

  @UiHandler("functionalUnitButton")
  void onAddUnit(ClickEvent event) {
    getUiHandlers().addUnit();
  }

  @UiHandler("exportButton")
  void onExportIdentifiers(ClickEvent event) {
    getUiHandlers().exportIdentifiers();
  }

  @UiHandler("importButton")
  void onImportIdentifiers(ClickEvent event) {
    getUiHandlers().importIdentifiers();
  }

  @UiHandler("syncButton")
  void onSynchronizeIdentifiers(ClickEvent event) {
    getUiHandlers().synchronizeIdentifiers();
  }
}
