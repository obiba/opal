/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.navigator.view;

import org.obiba.opal.web.gwt.app.client.navigator.presenter.NavigatorPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.CloseableList;
import org.obiba.opal.web.gwt.app.client.workbench.view.ListItem;
import org.obiba.opal.web.gwt.app.client.workbench.view.SuggestListBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.VariableSearchListItem;
import org.obiba.opal.web.gwt.app.client.workbench.view.VariableSuggestOracle;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.UIObjectAuthorizer;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class NavigatorView extends Composite implements NavigatorPresenter.Display {

  public static final int PAGE_SIZE = 100;

  @UiTemplate("NavigatorView.ui.xml")
  interface NavigatorViewUiBinder extends UiBinder<Widget, NavigatorView> {}

  private static final NavigatorViewUiBinder uiBinder = GWT.create(NavigatorViewUiBinder.class);

  @UiField
  Panel navigatorDisplayPanel;

  @UiField
  Panel breadcrumb;

  @UiField
  Button createDatasourceButton;

  @UiField
  Button importDataButton;

  @UiField
  Button exportDataButton;

  @UiField
  Button refreshButton;

  @UiField(provided = true)
  SuggestListBox search;

  private VariableSuggestOracle oracle;

  @Inject
  public NavigatorView(EventBus eventBus) {
    oracle = new VariableSuggestOracle(eventBus);
    search = new SuggestListBox(oracle);
    initWidget(uiBinder.createAndBindUi(this));

    search.addItemRemovedHandler(new CloseableList.ItemRemovedHandler() {
      @Override
      public void onItemRemoved(ListItem item) {
        VariableSearchListItem.ItemType type = ((VariableSearchListItem) item).getType();
        if(VariableSearchListItem.ItemType.DATASOURCE.equals(type)) {
          oracle.setDatasource(null);
        } else if(VariableSearchListItem.ItemType.TABLE.equals(type)) {
          oracle.setTable(null);
        }
      }
    });
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void addToSlot(Object slot, Widget content) {
    if(slot == NavigatorPresenter.LEFT_PANE) {
      breadcrumb.add(content);
    } else {
      navigatorDisplayPanel.add(content);
    }
  }

  @Override
  public void removeFromSlot(Object slot, Widget content) {
    if(slot == NavigatorPresenter.LEFT_PANE) {
      breadcrumb.remove(content);
    } else {
      navigatorDisplayPanel.remove(content);
    }
  }

  @Override
  public void setInSlot(Object slot, Widget content) {
    if(slot == NavigatorPresenter.LEFT_PANE) {
      breadcrumb.clear();
      breadcrumb.add(content);
    } else {
      navigatorDisplayPanel.clear();
      navigatorDisplayPanel.add(content);
    }
  }

  @Override
  public HandlerRegistration addCreateDatasourceClickHandler(ClickHandler handler) {
    return createDatasourceButton.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addExportDataClickHandler(ClickHandler handler) {
    return exportDataButton.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addImportDataClickHandler(ClickHandler handler) {
    return importDataButton.addClickHandler(handler);
  }

  @Override
  public HasAuthorization getCreateDatasourceAuthorizer() {
    return new UIObjectAuthorizer(createDatasourceButton);
  }

  @Override
  public HasAuthorization getImportDataAuthorizer() {
    return new UIObjectAuthorizer(importDataButton);
  }

  @Override
  public HasAuthorization getExportDataAuthorizer() {
    return new UIObjectAuthorizer(exportDataButton);
  }

  @Override
  public HandlerRegistration refreshClickHandler(ClickHandler handler) {
    return refreshButton.addClickHandler(handler);
  }

  @Override
  public void addSearchItem(String text, VariableSearchListItem.ItemType type) {
    String qText = quoteIfContainsSpace(text);
    if (VariableSearchListItem.ItemType.DATASOURCE.equals(type)) {
      oracle.setDatasource(qText);
    }
    if (VariableSearchListItem.ItemType.TABLE.equals(type)) {
      oracle.setTable(qText);
    }
    search.addItem(qText, type);
  }

  private String quoteIfContainsSpace(String s) {
    return s.contains(" ") ? "\"" + s + "\"" : s;
  }

  @Override
  public void clearSearch() {
    search.clear();
  }

  @Override
  public HandlerRegistration addSearchSelectionHandler(final SelectionHandler<SuggestOracle.Suggestion> handler) {
    return search.getSuggestBox().addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
      @Override
      public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
        // Reset suggestBox text to user input text
        String originalQuery = oracle.getOriginalQuery();
        search.getSuggestBox().setText(originalQuery);
        // Forward selection event
        handler.onSelection(event);
      }
    });
  }

}
