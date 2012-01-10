/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.view;

import org.obiba.opal.web.gwt.app.client.administration.presenter.AdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.HorizontalTabLayout;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.Tab;
import com.gwtplatform.mvp.client.TabData;

public class AdministrationView implements AdministrationPresenter.Display {

  private class AdminTab implements Tab {

    private final TabData tabData;

    private final SimplePanel tabContent;

    private final int tabIndex;

    private AdminTab(TabData tabData, String token) {
      this.tabData = tabData;
      this.tabContent = new SimplePanel();
      Hyperlink hl = new Hyperlink();
      hl.setTargetHistoryToken(token);
      hl.setText(tabData.getLabel());
      this.tabIndex = administrationDisplays.add(tabContent, hl);
    }

    public int getTabIndex() {
      return tabIndex;
    }

    @Override
    public void activate() {
      administrationDisplays.selectTab(tabContent);
    }

    @Override
    public Widget asWidget() {
      return tabContent;
    }

    @Override
    public void deactivate() {
    }

    @Override
    public float getPriority() {
      return tabData.getPriority();
    }

    @Override
    public String getText() {
      return tabData.getLabel();
    }

    @Override
    public void setTargetHistoryToken(String historyToken) {

    }

    @Override
    public void setText(String text) {

    }

  }

  @UiTemplate("AdministrationView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, AdministrationView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final Widget widget;

  @UiField
  HorizontalTabLayout administrationDisplays;

  private Widget currentTabContent;

  public AdministrationView() {
    super();
    widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public Tab addTab(final TabData tabData, final String historyToken) {
    return new AdminTab(tabData, historyToken);
  }

  @Override
  public void removeTab(Tab tab) {
    administrationDisplays.remove(((AdminTab) tab).getTabIndex());
  }

  @Override
  public void removeTabs() {
    administrationDisplays.clear();
  }

  @Override
  public void setActiveTab(Tab tab) {
    AdminTab adminTab = (AdminTab) tab;
    adminTab.tabContent.clear();
    adminTab.tabContent.add(currentTabContent);
    administrationDisplays.selectTab(adminTab.getTabIndex());
    currentTabContent = null;
  }

  @Override
  public void addToSlot(Object slot, Widget content) {
  }

  @Override
  public void removeFromSlot(Object slot, Widget content) {
  }

  @Override
  public void setInSlot(Object slot, Widget content) {
    if(slot == AdministrationPresenter.TabSlot) {
      currentTabContent = content;
    }
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

}
