package org.obiba.opal.web.gwt.app.client.ui.celltable;

import com.google.gwt.user.cellview.client.Column;

public class ActionsColumn<T> extends Column<T, T> implements HasActionHandler<T> {

  public static final String REMOVE_ACTION = "Remove";

  public static final String EDIT_ACTION = "Edit";

  public ActionsColumn(ActionHandler<T> handler) {
    this(EDIT_ACTION, REMOVE_ACTION);
    setActionHandler(handler);
  }

  public ActionsColumn(String... actions) {
    this(new ConstantActionsProvider<T>(actions));
  }

  public ActionsColumn(ActionsProvider<T> actionsProvider) {
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
