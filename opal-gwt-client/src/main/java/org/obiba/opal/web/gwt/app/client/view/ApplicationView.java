/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.view;

import com.github.gwtbootstrap.client.ui.*;
import com.google.gwt.core.client.GWT;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.CloseableList;
import org.obiba.opal.web.gwt.app.client.ui.ListItem;
import org.obiba.opal.web.gwt.app.client.ui.SuggestListBox;
import org.obiba.opal.web.gwt.app.client.ui.VariableSearchListItem;
import org.obiba.opal.web.gwt.app.client.ui.VariableSuggestOracle;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;

import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.base.Strings;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.NamedFrame;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 *
 */
public class ApplicationView extends ViewWithUiHandlers<ApplicationUiHandlers> implements ApplicationPresenter.Display {

  interface Binder extends UiBinder<Widget, ApplicationView> {}

  @UiField
  NavLink dashboardItem;

  @UiField
  NavLink administrationItem;

  @UiField
  NavLink profileItem;

  @UiField
  Dropdown username;

  @UiField
  Label version;

  @UiField
  NavLink projectsItem;

  @UiField
  Badge cartCounts;

  @UiField
  NavLink searchItem;

  @UiField
  NavWidget cartItem;

  @UiField
  Panel notification;

  @UiField
  Panel workbench;

  @UiField(provided = true)
  SuggestListBox search;

  @UiField
  Button resizeScreen;

  @UiField
  Tooltip resizeTooltip;

  @UiField
  FluidContainer workbenchContainer;

  @UiField
  FluidContainer footerContainer;

  @UiField
  Brand applicationName;

  @UiField
  FlowPanel panel;

  private FormPanel form;

  private NamedFrame frame;

  private TextBox xFileKey;

  private final VariableSuggestOracle oracle;

  @Inject
  public ApplicationView(EventBus eventBus, Binder uiBinder, Translations translations) {
    oracle = new VariableSuggestOracle(eventBus);
    oracle.setLimit(10);
    search = new SuggestListBox(oracle);
    search.getTextBox().setPlaceholder(translations.quickSearchVariablesTitle());
    initWidget(uiBinder.createAndBindUi(this));

    dashboardItem.setHref("#" + Places.DASHBOARD);
    projectsItem.setHref("#" + Places.PROJECTS);
    searchItem.setHref("#" + Places.SEARCH);
    cartItem.setHref("#" + Places.CART);
    administrationItem.setHref("#" + Places.ADMINISTRATION);
    profileItem.setHref("#" + Places.PROFILE);

    resizeTooltip.setText(translations.switchScreenDisplay());

    initCart();
    initSearchWidget();
    initDownloadWidgets();
  }

  private void initCart() {
    cartCounts.setText(String.valueOf(0));
    cartCounts.setVisible(false);
  }

  private void initSearchWidget() {
    search.addItemRemovedHandler(new CloseableList.ItemRemovedHandler() {
      @Override
      public void onItemRemoved(ListItem item) {
        VariableSearchListItem.ItemType type = ((VariableSearchListItem) item).getType();
        if(VariableSearchListItem.ItemType.DATASOURCE == type) {
          oracle.setDatasource(null);
        } else if(VariableSearchListItem.ItemType.TABLE == type) {
          oracle.setTable(null);
        }
      }
    });

    search.setUpdaterCallback(new Typeahead.UpdaterCallback() {
      @Override
      public String onSelection(SuggestOracle.Suggestion selectedSuggestion) {
        // Reset suggestBox text to user input text
        String originalQuery = oracle.getOriginalQuery();
        // Forward selection event
        if (((VariableSuggestOracle.Identifiable) selectedSuggestion).getId().equals("_advanced")) {
          getUiHandlers().onSearch((VariableSuggestOracle.AdvancedSearchSuggestion) selectedSuggestion);
        } else
          getUiHandlers().onSelection((VariableSuggestOracle.VariableSuggestion) selectedSuggestion);
        return originalQuery;
      }
    });
  }

  private void initDownloadWidgets() {
    frame = new NamedFrame("frame");
    frame.setVisible(false);
    xFileKey = new TextBox();
    xFileKey.setName("key");
    form = new FormPanel(frame);
    form.add(xFileKey);
    form.setVisible(false);
    panel.add(form);
    panel.add(frame);
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if(ApplicationPresenter.WORKBENCH == slot) {
      workbench.clear();
      workbench.add(content.asWidget());
    } else {
      notification.clear();
      notification.add(content);
    }
  }

  @Override
  public void setDownloadInfo(String url, String password) {
    if (!Strings.isNullOrEmpty(password)) {
      xFileKey.setText(password);
      form.setAction(url);
      form.setMethod(FormPanel.METHOD_POST);
      form.submit();
    } else {
      frame.setUrl(url);
    }
  }

  @Override
  public HasAuthorization getAdministrationAuthorizer() {
    return new WidgetAuthorizer(administrationItem);
  }

  @Override
  public void setUsername(String username) {
    this.username.setText(username);
  }

  @Override
  public void setVersion(String version) {
    this.version.setText(version);
  }

  @Override
  public void setCartCounts(int count) {
    cartCounts.setText(String.valueOf(count));
    cartCounts.setVisible(true);
  }

  @Override
  public void addSearchItem(String text, VariableSearchListItem.ItemType type) {
    String qText = quoteIfContainsSpace(text);
    if(VariableSearchListItem.ItemType.DATASOURCE == type) {
      oracle.setDatasource(qText);
    }
    if(VariableSearchListItem.ItemType.TABLE == type) {
      oracle.setTable(qText);
    }
    search.addItem(qText, new VariableSearchListItem(type, qText));
  }

  private String quoteIfContainsSpace(String s) {
    return s.contains(" ") ? "\"" + s + "\"" : s;
  }

  @Override
  public void clearSearch() {
    search.clear();
  }

  @Override
  public void setApplicationName(String text) {
    applicationName.setText(text);
    if (Document.get() != null) {
      Document.get().setTitle (text);
    }
  }

  @UiHandler("quitItem")
  void onQuit(ClickEvent event) {
    getUiHandlers().onQuit();
  }

  @UiHandler("resizeScreen")
  void onResize(ClickEvent event) {
    boolean isFullScreen = workbenchContainer.getStyleName().contains("almost-full-width");
    if(isFullScreen) {
      workbenchContainer.removeStyleName("almost-full-width");
      footerContainer.removeStyleName("almost-full-width");
      resizeScreen.setIcon(IconType.RESIZE_FULL);
    } else {
      workbenchContainer.addStyleName("almost-full-width");
      footerContainer.addStyleName("almost-full-width");
      resizeScreen.setIcon(IconType.RESIZE_SMALL);
    }
  }

}
