package org.obiba.opal.web.gwt.app.client.widgets.tab;

import java.util.HashMap;
import java.util.Map;

import com.github.gwtbootstrap.client.ui.TabPanel;

public class OpalTabPanel extends TabPanel {

  private Map<Integer, Object> data = new HashMap<Integer, Object>();

  public OpalTabPanel setData(int index, Object data) {
    this.data.put(index, data);
    return this;
  }

  public Object getData(int index) {
    return data.get(index);
  }
}
