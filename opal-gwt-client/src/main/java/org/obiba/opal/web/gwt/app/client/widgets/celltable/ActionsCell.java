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

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 *
 */
public class ActionsCell<T> extends AbstractCell<T> implements HasActionHandler<T> {

  private static final Translations translations = GWT.create(Translations.class);

  private final ActionsProvider<T> actionsProvider;

  // Every action shares this Cell instance
  private final Cell<String> actionCell;

  private final FieldUpdater<T, String> hasCellFieldUpdater;

  private ActionHandler<T> actionHandler;

  private final CompositeCell<T> delegate;

  public ActionsCell(ActionsProvider<T> actionsProvider) {
    super("click", "keydown");
    this.actionsProvider = actionsProvider;

    actionCell = new ClickableTextCell(new LocalisedSafeHtmlRenderer(translations.actionMap()));

    hasCellFieldUpdater = new FieldUpdater<T, String>() {
      public void update(int rowIndex, T object, String value) {

        // Value can be null when an action is not available for a particular row
        if(value != null && actionHandler != null) {
          actionHandler.doAction(object, value);
        }
      }
    };
    delegate = createCompositeCell(actionsProvider.allActions());
  }

  @Override
  public void render(Context context, T value, SafeHtmlBuilder sb) {
    delegate.render(context, value, sb);
  }

  @Override
  public void onBrowserEvent(Context context, Element parent, T value, NativeEvent event,
      ValueUpdater<T> valueUpdater) {
    delegate.onBrowserEvent(context, parent, value, event, valueUpdater);
  }

  public void setActionHandler(ActionHandler<T> actionHandler) {
    this.actionHandler = actionHandler;
  }

  private CompositeCell<T> createCompositeCell(String... actionNames) {

    List<HasCell<T, ?>> hasCells = new ArrayList<HasCell<T, ?>>();

    for(final String actionName : actionNames) {
      hasCells.add(new Action(actionName));
    }

    return new CompositeCell<T>(hasCells);
  }

  private class Action implements HasCell<T, String> {

    private final String actionName;

    Action(String name) {
      this.actionName = name;
    }

    @Override
    public Cell<String> getCell() {
      return actionCell;
    }

    @Override
    public FieldUpdater<T, String> getFieldUpdater() {
      return hasCellFieldUpdater;
    }

    @Override
    public String getValue(T object) {
      String[] actions = actionsProvider.getActions(object);
      for(String a : actions) {
        if(a.equals(actionName)) {
          return actionName;
        }
      }
      return null;
    }

  }

}
