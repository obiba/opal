/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.system.news;

import java.util.List;

public class ObibaNews {
  private int summary;
  private List<ObibaNewsDatum> data;

  public ObibaNews() {
  }

  public void setSummary(int summary) {
    this.summary = summary;
  }

  public int getSummary() {
    return summary;
  }

  public void setData(List<ObibaNewsDatum> data) {
    this.data = data;
  }

  public List<ObibaNewsDatum> getData() {
    return data;
  }
}
