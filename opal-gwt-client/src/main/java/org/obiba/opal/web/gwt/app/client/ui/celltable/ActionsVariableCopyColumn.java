/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui.celltable;

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
