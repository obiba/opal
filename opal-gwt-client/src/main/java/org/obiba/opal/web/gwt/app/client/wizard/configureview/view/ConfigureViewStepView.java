/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.configureview.view;

import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.ConfigureViewStepPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.HorizontalTabLayout;
import org.obiba.opal.web.gwt.app.client.workbench.view.ResizeHandle;
import org.obiba.opal.web.gwt.app.client.workbench.view.Tooltip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

public class ConfigureViewStepView extends PopupViewImpl implements ConfigureViewStepPresenter.Display {

  @UiTemplate("ConfigureViewStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<DialogBox, ConfigureViewStepView> {}

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  @UiField
  DialogBox dialog;

  @UiField
  DockLayoutPanel contentLayout;

  @UiField
  ResizeHandle resizeHandle;

  @UiField
  DeckPanel helpPanelDecks;

  @UiField
  SimplePanel dataTabPanel;

  @UiField
  SimplePanel variablesTabPanel;

  @UiField
  HorizontalTabLayout viewTabs;

  @UiField
  Button close;

  @UiField
  Button help;

  @Inject
  public ConfigureViewStepView(EventBus eventBus) {
    super(eventBus);
    uiBinder.createAndBindUi(this);
    resizeHandle.makeResizable(contentLayout);
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
    return dialog;
  }

  @Override
  public void setInSlot(Object slot, Widget content) {
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
  public HorizontalTabLayout getViewTabs() {
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
