package org.obiba.opal.web.gwt.app.client.ui;

import com.google.gwt.cell.client.FieldUpdater;

public interface HasFieldUpdater<T, C> {

  FieldUpdater<T, C> getFieldUpdater();

  void setFieldUpdater(FieldUpdater<T, C> fieldUpdater);

}