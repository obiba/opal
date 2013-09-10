/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.configureview.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.configureview.presenter.ConfigureViewStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.configureview.presenter.ConfigureViewStepUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.NavTabsPanel;
import org.obiba.opal.web.gwt.app.client.ui.Tooltip;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class ConfigureViewStepView extends ModalPopupViewWithUiHandlers<ConfigureViewStepUiHandlers>
    implements ConfigureViewStepPresenter.Display {

  interface ViewUiBinder extends UiBinder<Widget, ConfigureViewStepView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  Modal dialog;

  @UiField
  DeckPanel helpPanelDecks;

  @UiField
  SimplePanel dataTabPanel;

  @UiField
  SimplePanel variablesTabPanel;

  @UiField
  NavTabsPanel viewTabs;

  @UiField
  Button close;

  @UiField
  Button help;

  @Inject
  public ConfigureViewStepView(EventBus eventBus) {
    super(eventBus);
    widget = uiBinder.createAndBindUi(this);
    dialog.setTitle(translations.configureViewModalTitle());
    initHelpTooltip();
  }

  private void initHelpTooltip() {
    final Tooltip helpTooltip = new Tooltip();
    help.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent evt) {
        helpTooltip.setPopupPosition(evt.getNativeEvent().getClientX() + 20, evt.getNativeEvent().getClientY() - 200);
        helpTooltip.setSize("300px", "200px");
        helpTooltip.show();
      }
    });
    helpTooltip.add(helpPanelDecks);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    Slots s = (Slots) slot;
    switch(s) {
      case Variables:
        variablesTabPanel.clear();
        variablesTabPanel.add(content);
    }
  }

  @Override
  public DeckPanel getHelpDeck() {
    return helpPanelDecks;
  }

  @Override
  public NavTabsPanel getViewTabs() {
    return viewTabs;
  }

  @Override
  public void addDataTabWidget(Widget widget) {
    dataTabPanel.clear();
    dataTabPanel.add(widget);
  }

  @Override
  public void displayTab(int tabNumber) {
    viewTabs.selectTab(tabNumber);
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public HandlerRegistration addCloseClickHandler(ClickHandler handler) {
    return close.addClickHandler(handler);
  }

}
