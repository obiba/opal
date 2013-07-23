package org.obiba.opal.web.gwt.app.client.widgets.tab;

public interface HasTabPanel {
  void selectTab(int index);
  void setTabData(int index, Object data);
  Object getTabData(int index);
}
