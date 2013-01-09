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

  private MenuItem importDataItem;

  private MenuItem exportDataItem;

  private MenuItem copyDataItem;

  private MenuItem viewDownloadItem;

  private MenuItem addItem;

  private MenuItem addViewItem;

  private MenuItem addUpdateTablesItem;

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
      addItem = new MenuItem("", addMenu);
      addItem.addStyleName("add");
      addItem(addItem);
    }
    return this;
  }

  public MenuItem getAddItem() {
    return addItem;
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

  public MenuItem getExcelDownloadItem() {
    return excelDownloadItem;
  }

  public void setImportDataCommand(Command cmd) {
    if(importDataItem == null) {
      importDataItem = new MenuItem(translations.importData(), cmd);
      getAddMenu().addItem(importDataItem);
    } else {
      importDataItem.setCommand(cmd);
    }
  }

  public void setExportDataCommand(Command cmd) {
    if(exportDataItem == null) {
      exportDataItem = new MenuItem(translations.exportData(), cmd);
      getToolsMenu().addItem(exportDataItem);
    } else {
      exportDataItem.setCommand(cmd);
    }
  }

  public MenuItem getImportDataItem() {
    return importDataItem;
  }

  public MenuItem getExportDataItem() {
    return exportDataItem;
  }

  public void setCopyDataCommand(Command cmd) {
    if(copyDataItem == null) {
      copyDataItem = new MenuItem(translations.copyData(), cmd);
      getToolsMenu().addItem(copyDataItem);
    } else {
      copyDataItem.setCommand(cmd);
    }
  }

  public MenuItem getCopyDataItem() {
    return copyDataItem;
  }

  public void setViewDownloadCommand(Command cmd) {
    if(viewDownloadItem == null) {
      viewDownloadItem = new MenuItem(translations.downloadViewXML(), cmd);
      getToolsMenu().addItem(viewDownloadItem);
    } else {
      viewDownloadItem.setCommand(cmd);
    }
  }

  public void removeViewDownloadCommand() {
    if(viewDownloadItem != null) {
      getToolsMenu().removeItem(viewDownloadItem);
      viewDownloadItem = null;
    }
  }

  public MenuItem getViewDownloadItem() {
    return viewDownloadItem;
  }

  public void setAddViewCommand(Command cmd) {
    if(addViewItem == null) {
      addViewItem = new MenuItem(translations.addViewLabel(), cmd);
      getAddMenu().addItem(addViewItem);
    } else {
      addViewItem.setCommand(cmd);
    }
  }

  public void setAddUpdateTablesCommand(Command cmd) {
    if(addUpdateTablesItem == null) {
      addUpdateTablesItem = new MenuItem(translations.addUpdateTablesLabel(), cmd);
      getAddMenu().addItem(addUpdateTablesItem);
    } else {
      addUpdateTablesItem.setCommand(cmd);
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

  public MenuItem getAddUpdateTablesItem() {
    return addUpdateTablesItem;
  }

  public MenuItem getAddViewItem() {
    return addViewItem;
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

  public MenuItem getEditItem() {
    return editItem;
  }

  public void setExportDataItemEnabled(boolean enabled) {
    if(exportDataItem != null) {
      exportDataItem.setEnabled(enabled);
    }
  }

  public void setCopyDataItemEnabled(boolean enabled) {
    if(copyDataItem != null) {
      copyDataItem.setEnabled(enabled);
    }
  }
}
