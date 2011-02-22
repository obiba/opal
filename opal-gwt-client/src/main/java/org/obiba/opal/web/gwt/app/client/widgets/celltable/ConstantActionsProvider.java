/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.celltable;


/**
 *
 */
public final class ConstantActionsProvider<T> implements ActionsProvider<T> {

  private final String[] actions;

  public ConstantActionsProvider(final String... actions) {
    this.actions = actions;
  }

  @Override
  public String[] allActions() {
    return actionsCopy();
  }

  @Override
  public String[] getActions(T value) {
    return actionsCopy();
  }

  private String[] actionsCopy() {
    String[] copy = new String[actions.length];
    for(int i = 0; i < actions.length; i++) {
      copy[i] = actions[i];
    }
    return copy;
  }

}
