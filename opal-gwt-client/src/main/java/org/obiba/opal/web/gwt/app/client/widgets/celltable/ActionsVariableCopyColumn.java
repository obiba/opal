package org.obiba.opal.web.gwt.app.client.widgets.celltable;

import com.google.gwt.user.cellview.client.Column;

public class ActionsVariableCopyColumn<T> extends Column<T, T> implements HasActionHandler<T> {

  public static final String REMOVE_ACTION = "Remove";

  public ActionsVariableCopyColumn(String... actions) {
    this(new ConstantActionsProvider<T>(actions));
  }

  public ActionsVariableCopyColumn(ActionsProvider<T> actionsProvider) {
    super(new ActionsCell<T>(actionsProvider));
    setCellStyleNames("row-actions");
  }

  @Override
  public T getValue(T object) {
    return object;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void setActionHandler(ActionHandler<T> actionHandler) {
    ((HasActionHandler<T>) getCell()).setActionHandler(actionHandler);
  }
}
