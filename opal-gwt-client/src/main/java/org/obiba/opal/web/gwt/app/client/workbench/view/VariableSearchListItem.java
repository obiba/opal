package org.obiba.opal.web.gwt.app.client.workbench.view;

public class VariableSearchListItem extends ListItem {

  public enum ItemType {
    DATASOURCE,
    TABLE
  }

  private ItemType type;

  public VariableSearchListItem(ItemType type) {
    this.type = type;
  }

  public void setType(ItemType type) {
    this.type = type;
  }

  public ItemType getType() {
    return type;
  }

}
