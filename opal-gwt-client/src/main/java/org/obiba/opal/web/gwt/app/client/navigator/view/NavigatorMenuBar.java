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

import org.obiba.opal.web.gwt.app.client.i18n.Translations;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;

/**
 *
 */
public class NavigatorMenuBar extends MenuBar {

  private MenuItem previousItem;

  private MenuItem nextItem;

  private MenuItem parentItem;

  private MenuBar toolsMenu;

  private MenuItem excelDownloadItem;

  private MenuItem downloadViewItem;

  private MenuItem addViewItem;

  private MenuBar addMenu;

  private MenuItem editItem;

  private Translations translations = GWT.create(Translations.class);

  private boolean separated = false;

  public NavigatorMenuBar() {
    super();
    setAutoOpen(true);

    previousItem = new MenuItem("", (Command) null);
    previousItem.addStyleName("previous");
    addItem(previousItem);

    nextItem = new MenuItem("", (Command) null);
    nextItem.addStyleName("next");
    addItem(nextItem);

    parentItem = new MenuItem("", (Command) null);
    parentItem.addStyleName("parent");
    addItem(parentItem);
  }

  private void ensureSeparation() {
    if(!separated) {
      addSeparator();
      separated = true;
    }
  }

  public NavigatorMenuBar withToolsMenu() {
    if(toolsMenu == null) {
      ensureSeparation();
      toolsMenu = new MenuBar(true);
      MenuItem toolsItem = new MenuItem("", toolsMenu);
      toolsItem.addStyleName("tools");
      addItem(toolsItem);
    }
    return this;
  }

  public NavigatorMenuBar withAddMenu() {
    if(addMenu == null) {
      ensureSeparation();
      addMenu = new MenuBar(true);
      MenuItem addItem = new MenuItem("", addMenu);
      addItem.addStyleName("add");
      addItem(addItem);
    }
    return this;
  }

  public void setParentName(String name) {
    if(name != null && name.length() > 0) {
      parentItem.removeStyleName("disabled");
    } else {
      parentItem.addStyleName("disabled");
    }
    parentItem.setTitle(name);
  }

  public void setNextName(String name) {
    if(name != null && name.length() > 0) {
      nextItem.removeStyleName("disabled");
    } else {
      nextItem.addStyleName("disabled");
    }
    nextItem.setTitle(name);
  }

  public void setPreviousName(String name) {
    if(name != null && name.length() > 0) {
      previousItem.removeStyleName("disabled");
    } else {
      previousItem.addStyleName("disabled");
    }
    previousItem.setTitle(name);
  }

  public void setParentCommand(Command cmd) {
    parentItem.setCommand(cmd);
  }

  public void setNextCommand(Command cmd) {
    nextItem.setCommand(cmd);
  }

  public void setPreviousCommand(Command cmd) {
    previousItem.setCommand(cmd);
  }

  public void setExcelDownloadCommand(Command cmd) {
    if(excelDownloadItem == null) {
      excelDownloadItem = new MenuItem(translations.exportToExcelTitle(), cmd);
      getToolsMenu().addItem(excelDownloadItem);
    } else {
      excelDownloadItem.setCommand(cmd);
    }
  }

  public void setDownloadViewCommand(Command cmd) {
    if(downloadViewItem == null) {
      downloadViewItem = new MenuItem(translations.downloadViewXML(), cmd);
      getToolsMenu().addItem(downloadViewItem);
    } else {
      downloadViewItem.setCommand(cmd);
    }
  }

  public void removeDownloadViewCommand() {
    if(downloadViewItem != null) {
      getToolsMenu().removeItem(downloadViewItem);
      downloadViewItem = null;
    }
  }

  public void setAddViewCommand(Command cmd) {
    if(addViewItem == null) {
      addViewItem = new MenuItem(translations.addViewLabel(), cmd);
      getAddMenu().addItem(addViewItem);
    } else {
      addViewItem.setCommand(cmd);
    }
  }

  public MenuBar getToolsMenu() {
    withToolsMenu();
    return toolsMenu;
  }

  public MenuBar getAddMenu() {
    withAddMenu();
    return addMenu;
  }

  public void setEditCommand(Command cmd) {
    if(cmd == null && editItem != null) {
      removeItem(editItem);
      editItem = null;
    } else if(cmd != null && editItem == null) {
      ensureSeparation();
      editItem = new MenuItem("", cmd);
      editItem.addStyleName("edit");
      addItem(editItem);
    } else if(cmd != null) {
      editItem.setCommand(cmd);
    }
  }

}
