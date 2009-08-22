package org.obiba.opal.jdbcmart.batch.naming;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.obiba.opal.elmo.concepts.DataItem;
import org.obiba.opal.jdbcmart.batch.IColumnNamingStrategy;
import org.obiba.opal.sesame.report.DataItemSet;

public class DefaultColumnNamingStrategy implements IColumnNamingStrategy {

  private int maxLengh = 64;

  private String prefix = "OPAL";

  private Map<String, String> codeToName = new HashMap<String, String>();

  private Map<String, String> trims = new LinkedHashMap<String, String>();

  public DefaultColumnNamingStrategy() {
  }

  public void setMaxLengh(int maxLengh) {
    this.maxLengh = maxLengh;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }
  
  public void setTrims(Map<String, String> trims) {
    this.trims = trims;
  }

  public String getColumnName(DataItem item) {
    if(codeToName.size() > 1000) {
      return getDefaultName(item);
    }
    return trimName(codeToName.get(item.getIdentifier()));
  }

  public void prepare(DataItemSet dataItemSet) {
    Map<String, ItemName> names = new HashMap<String, ItemName>();

    codeToName.clear();
    for(DataItem item : dataItemSet.getDataItems()) {
      ItemName itemName = new ItemName(item);
      String currentName = itemName.getCurrentName();

      // Handle the existence of another item with the same name
      while(names.containsKey(currentName)) {
        // Get the conflicting item
        ItemName other = names.get(currentName);
        // Can we resolve this conflict?
        if(other.hasMoreParts() == false && itemName.hasMoreParts() == false) {
          // No we can't, use the default name
          currentName = getDefaultName(itemName.dataItem);
        } else {
          if(other.hasMoreParts()) {
            // Don't remove the previous entry. This is done so that if a conflict involves more than two items, then
            // the subsequent ones will also be conflicting and need their name to be "incremented"
            // names.remove(currentName);

            // "increment" the name and put it back in the map
            names.put(other.addPart(), other);
          }
          if(itemName.hasMoreParts()) {
            // "increment" this name also.
            currentName = itemName.addPart();
          }
        }
        // iterate
      }
      // No conflict, this item "owns" the name
      names.put(currentName, itemName);
    }
    // Build the map of unique names
    for(ItemName item : names.values()) {
      String name = item.getCurrentName();
      if(name.length() > maxLengh) {
        name = getDefaultName(item.dataItem);
      }
      codeToName.put(item.dataItem.getIdentifier(), name);
    }
  }

  protected String getDefaultName(DataItem item) {
    return prefix + item.getIdentifier();
  }

  protected String trimName(String name) {
    for(Map.Entry<String, String> entry : trims.entrySet()) {
      name = name.replaceAll(entry.getKey(), entry.getValue());
    }
    return name;
  }

  private class ItemName {

    private int partCount = 0;

    private String currentName = "";

    private DataItem dataItem;

    public ItemName(DataItem dataItem) {
      this.dataItem = dataItem;
      addPart();
    }

    public String getCurrentName() {
      return currentName;
    }

    public boolean hasMoreParts() {
      return parts().length > partCount;
    }

    public int partCount() {
      return partCount;
    }

    String addPart() {
      String[] parts = parts();
      StringBuilder sb = new StringBuilder(currentName);
      if(sb.length() > 0) {
        sb.insert(0, '.');
      }
      // Insert parts from right to left
      int partIndex = parts.length - 1 - partCount;
      if(partIndex < 0) {
        throw new IllegalStateException("No more parts in " + dataItem.getName() + ": " + currentName);
      }
      partCount++;
      sb.insert(0, parts[partIndex]);
      currentName = sb.toString();
      return getCurrentName();
    }

    String[] parts() {
      return dataItem.getName().split("\\.");
    }
  }
}
