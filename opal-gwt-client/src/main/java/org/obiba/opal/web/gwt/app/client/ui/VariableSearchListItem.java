/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

public class VariableSearchListItem extends ListItem {

  public enum ItemType {
    DATASOURCE,
    TABLE
  }

  private ItemType type;

  public VariableSearchListItem(ItemType type, String title) {
    this.type = type;
    setItemTitle(title);
  }

  public void setType(ItemType type) {
    this.type = type;
  }

  public ItemType getType() {
    return type;
  }

  private void setItemTitle(String title) {
    if(type == ItemType.DATASOURCE) {
      setTitle("project:" + title);
    } else {
      setTitle("table:" + title);
    }
  }

}
