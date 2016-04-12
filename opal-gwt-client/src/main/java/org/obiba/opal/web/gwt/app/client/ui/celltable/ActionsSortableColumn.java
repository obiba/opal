package org.obiba.opal.web.gwt.app.client.ui.celltable;

import java.util.List;

import com.google.common.base.Supplier;

public class ActionsSortableColumn<T> extends ActionsColumn<T> {

  public static final String MOVE_UP_ACTION = "MoveUp";

  public static final String MOVE_DOWN_ACTION = "MoveDown";

  public ActionsSortableColumn(Supplier<List<T>> supplier, ActionHandler<T> handler) {
    this(supplier, EDIT_ACTION, REMOVE_ACTION);
    setActionHandler(handler);
  }

  public ActionsSortableColumn(Supplier<List<T>> supplier, String... actions) {
    this(new SortableActionsProvider<T>(supplier, actions));
  }

  public ActionsSortableColumn(ActionsProvider<T> actionsProvider) {
    super(actionsProvider);
  }
}
