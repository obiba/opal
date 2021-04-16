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

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Supplier;

/**
 *
 */
public final class SortableActionsProvider<T> implements ActionsProvider<T> {

  private final String[] actions;
  private Supplier<List<T>> supplier;

  public SortableActionsProvider(Supplier<List<T>> supplier, String... actions) {
    this.actions = actions;
    this.supplier = supplier;
  }

  @Override
  public String[] allActions() {
    return concatArrays(actions,
        new String[] { ActionsSortableColumn.MOVE_UP_ACTION, ActionsSortableColumn.MOVE_DOWN_ACTION });
  }

  @Override
  public String[] getActions(T value) {
    if(supplier.get().size() < 2) {
      return actions;
    }

    return supplier.get().indexOf(value) == 0 ? getHeadActions() : //
        supplier.get().indexOf(value) == supplier.get().size() - 1 ? getTailActions() : allActions();
  }

  private String[] getHeadActions() {
    return concatArrays(actions, new String[] { ActionsSortableColumn.MOVE_DOWN_ACTION });
  }

  private String[] getTailActions() {
    return concatArrays(actions, new String[] { ActionsSortableColumn.MOVE_UP_ACTION });
  }

  private static String[] concatArrays(String[] first, String[] second) {
    String[] result = Arrays.copyOf(first, first.length + second.length);
    System.arraycopy(second, 0, result, first.length, second.length);
    return result;
  }
}
