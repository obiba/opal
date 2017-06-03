/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.search.entities;

import com.github.gwtbootstrap.client.ui.*;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.search.entity.SearchEntityPresenter;
import org.obiba.opal.web.gwt.app.client.search.entity.SearchEntityUiHandlers;
import org.obiba.opal.web.gwt.app.client.search.entity.ValueSetTable;
import org.obiba.opal.web.gwt.app.client.search.entity.VariableValueRow;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.TableChooser;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableEntitySummaryDto;

import java.util.List;
import java.util.Map;

public class SearchEntitiesView extends ViewWithUiHandlers<SearchEntitiesUiHandlers> implements SearchEntitiesPresenter.Display {

  interface Binder extends UiBinder<Widget, SearchEntitiesView> {}


  private final PlaceManager placeManager;

  @UiField
  Breadcrumbs breadcrumbs;

  @UiField
  Panel entityPanel;

  @UiField
  DropdownButton typeDropdown;

  @UiField
  TextBox queryInput;

  @UiField
  Panel entityResultPanel;

  @UiField
  Heading entityTitle;

  @UiField
  Image refreshPending;

  private List<VariableValueRow> variableValueRows;

  private ListDataProvider<VariableValueRow> valueSetProvider = new ListDataProvider<VariableValueRow>();

  @Inject
  public SearchEntitiesView(SearchEntitiesView.Binder uiBinder, Translations translations, PlaceManager placeManager) {
    initWidget(uiBinder.createAndBindUi(this));
    this.placeManager = placeManager;
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @UiHandler("searchButton")
  public void onSearch(ClickEvent event) {
    if (queryInput.getValue().isEmpty()) reset();
    else getUiHandlers().onSearch(typeDropdown.getText().trim(), queryInput.getValue());
  }

  @Override
  public void setEntityTypes(List<VariableEntitySummaryDto> entityTypes, String selectedType) {
    typeDropdown.clear();
    String selectedEntityType = Strings.isNullOrEmpty(selectedType) ? "Participant" : selectedType;
    boolean hasSelectedEntityType = false;
    boolean hasParticipantType = false;
    for (VariableEntitySummaryDto typeSummary : entityTypes) {
      final NavLink item = new NavLink(typeSummary.getEntityType());
      item.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
         typeDropdown.setText(item.getText());
        }
      });
      typeDropdown.add(item);
      if (selectedEntityType.equals(typeSummary.getEntityType())) hasSelectedEntityType = true;
      if ("Participant".equals(typeSummary.getEntityType())) hasParticipantType = true;
    }
    if (hasSelectedEntityType) typeDropdown.setText(selectedEntityType);
    else if (!entityTypes.isEmpty()) {
      if (hasParticipantType) typeDropdown.setText("Participant");
      else typeDropdown.setText(entityTypes.get(0).getEntityType());
    }
    entityPanel.setVisible(!entityTypes.isEmpty());
  }

  @Override
  public void setEntityType(String selectedType) {
    typeDropdown.setText(Strings.isNullOrEmpty(selectedType) ? "Participant" : selectedType);
  }

  @Override
  public void setQuery(String query) {
    queryInput.setValue(query);
    if (Strings.isNullOrEmpty(query)) clearResults(false);
  }

  @Override
  public void clearResults(boolean searchProgress) {
    entityResultPanel.setVisible(false);
    refreshPending.setVisible(searchProgress);
  }

  @Override
  public void reset() {
    clearResults(false);
    queryInput.setText("");
  }

}
