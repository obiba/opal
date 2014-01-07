package org.obiba.opal.web.gwt.app.client.ui;

public interface HasTabPanel {
  void selectTab(int index);

  void setTabData(int index, Object data);

  Object getTabData(int index);

  void clearTabsData();
}
