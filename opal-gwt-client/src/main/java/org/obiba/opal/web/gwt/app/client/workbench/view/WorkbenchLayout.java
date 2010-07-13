/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.workbench.view;

import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.WidgetCollection;

/**
 *
 */
public class WorkbenchLayout extends Composite implements HasWidgets {

  @UiTemplate("WorkbenchLayout.ui.xml")
  interface WorkbenchLayoutUiBinder extends UiBinder<Widget, WorkbenchLayout> {
  }

  private static WorkbenchLayoutUiBinder uiBinder = GWT.create(WorkbenchLayoutUiBinder.class);

  //
  // Instance Variables
  //

  @UiField
  DockLayoutPanel workbench;

  @UiField
  FlowPanel topHeader;

  @UiField
  SimplePanel controlContent;

  @UiField
  ScrollPanel mainContent;

  @UiField
  Image closeInfo;

  @UiField
  ScrollPanel informationContent;

  @UiField
  SplitLayoutPanel content;

  private WidgetCollection children = new WidgetCollection(this);

  private boolean withInfo;

  //
  // Constructors
  //

  public WorkbenchLayout() {
    this(true);
  }

  public WorkbenchLayout(boolean withInfo) {
    super();
    initWidget(uiBinder.createAndBindUi(this));
    setWithInfo(withInfo);
  }

  public void setWithInfo(boolean withInfo) {
    this.withInfo = withInfo;
    if(!withInfo) {
      mainContent.removeFromParent();
      workbench.remove(content);
      mainContent.setStyleName("content", true);
      workbench.add(mainContent);
    } else {
      closeInfo.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent arg0) {
          // TODO set slider position
        }
      });
      // http://code.google.com/p/google-web-toolkit/issues/detail?id=4489
      closeInfo.setVisible(false);
    }
  }

  public void setTitle(Label label) {
    label.setStyleName("title", true);
    topHeader.add(label);
  }

  public void setSummary(Label label) {
    topHeader.add(label);
  }

  public void setControl(Widget w) {
    controlContent.setWidget(w);
  }

  public void setMain(Widget w) {
    mainContent.setWidget(w);
  }

  public void setInformation(Widget w) {
    if(withInfo) {
      informationContent.setWidget(w);
    }
  }

  //
  // HasWidgets methods
  //

  @Override
  public void add(Widget w) {
    children.add(w);
    if(children.size() == 1) {
      setTitle((Label) w);
    } else if(children.size() == 2) {
      setSummary((Label) w);
    } else if(children.size() == 3) {
      setControl(w);
    } else if(children.size() == 4) {
      setMain(w);
    } else if(children.size() == 5) {
      setInformation(w);
    }
  }

  @Override
  public void clear() {
    for(int i = 0; i < children.size(); i++) {
      children.get(i).removeFromParent();
      children.remove(i);
    }
  }

  @Override
  public Iterator<Widget> iterator() {
    return children.iterator();
  }

  @Override
  public boolean remove(Widget w) {
    if(w.getParent() != this) {
      return false;
    }
    if(children.contains(w)) {
      children.get(children.indexOf(w)).removeFromParent();
      children.remove(w);
      return true;
    }
    return false;
  }
}
