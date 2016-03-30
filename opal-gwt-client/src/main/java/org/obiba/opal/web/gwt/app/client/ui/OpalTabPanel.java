package org.obiba.opal.web.gwt.app.client.ui;

import java.util.HashMap;
import java.util.Map;

import com.github.gwtbootstrap.client.ui.TabPanel;

public class OpalTabPanel extends TabPanel {

  private Map<Integer, Object> data = new HashMap<>();

  public OpalTabPanel setData(int index, Object value) {
    data.put(index, value);
    return this;
  }

  public Object getData(int index) {
    return data.get(index);
  }

  public void clearData() {
    data.clear();
  }
}
