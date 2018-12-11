package org.obiba.opal.web.gwt.app.client.ui;

import com.google.gwt.i18n.client.HasDirection;
import com.google.gwt.user.client.ui.ListBox;

import java.util.ArrayList;
import java.util.List;

public class OpalListBox extends ListBox {
  List<String> items = new ArrayList<String>();
  List<String> values = new ArrayList<String>();

  @Override
  public void insertItem(String item, HasDirection.Direction dir, String value, int index) {
    items.add(ensureInsertIndex(items, index), item);
    values.add(ensureInsertIndex(values, index), value);
    super.insertItem(item, dir, value, index);
  }

  @Override
  public void removeItem(int index) {
    checkIndex(index);

    items.remove(index);
    values.remove(index);
    super.removeItem(index);
  }

  public int getItemIndex(String item) {
    return items.indexOf(item);
  }

  public int getValueIndex(String item) {
    return values.indexOf(item);
  }

  protected int ensureInsertIndex(List<String> source, int index) {
    int size = source.size();
    return index < 0 || index > size ? size : index;
  }

  protected void checkIndex(int index) {
    if (index < 0 || index >= getItemCount()) {
      throw new IndexOutOfBoundsException();
    }
  }
}
