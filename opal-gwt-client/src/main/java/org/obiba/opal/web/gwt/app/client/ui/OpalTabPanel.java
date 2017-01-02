/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import java.util.HashMap;
import java.util.Map;

import com.github.gwtbootstrap.client.ui.TabPanel;

public class OpalTabPanel extends TabPanel {

  private Map<Integer, Object> data = new HashMap<Integer, Object>();

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
