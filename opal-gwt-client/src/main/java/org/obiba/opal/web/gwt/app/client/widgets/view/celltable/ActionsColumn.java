/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.view.celltable;

import static org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.LocalizablesPresenter.DELETE_ACTION;
import static org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.LocalizablesPresenter.EDIT_ACTION;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;

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
import com.google.gwt.user.cellview.client.Column;

/**
 *
 */
public class ActionsColumn<T> extends Column<T, T> implements HasActionHandler<T> {
  //
  // Static Variables
  //

  private static Translations translations = GWT.create(Translations.class);

  //
  // Constructors
  //

  public ActionsColumn() {
    super(new ActionsCell<T>());
  }

  //
  // Column Methods
  //

  public T getValue(T object) {
    return object;
  }

  //
  // HasActionHandler Methods
  //

  public void setActionHandler(ActionHandler<T> actionHandler) {
    ((ActionsCell<T>) getCell()).setActionHandler(actionHandler);
  }

  //
  // Inner Classes / Interfaces
  //

  static class ActionsCell<T> extends AbstractCell<T> {
    //
    // Instance Variables
    //

    private CompositeCell<T> delegateCell;

    private FieldUpdater<T, String> hasCellFieldUpdater;

    private ActionHandler<T> actionHandler;

    //
    // Constructors
    //

    public ActionsCell() {
      hasCellFieldUpdater = new FieldUpdater<T, String>() {
        public void update(int rowIndex, T object, String value) {
          if(actionHandler != null) {
            actionHandler.doAction(object, value);
          }
        }
      };
    }

    //
    // AbstractCell Methods
    //

    @Override
    public Object onBrowserEvent(Element parent, T value, Object viewData, NativeEvent event, ValueUpdater<T> valueUpdater) {
      refreshActions(value);

      return delegateCell.onBrowserEvent(parent, value, viewData, event, valueUpdater);
    }

    @Override
    public void render(T value, Object viewData, StringBuilder sb) {
      refreshActions(value);

      delegateCell.render(value, viewData, sb);
    }

    //
    // Methods
    //

    public void setActionHandler(ActionHandler<T> actionHandler) {
      this.actionHandler = actionHandler;
    }

    private void refreshActions(T value) {
      delegateCell = createCompositeCell(EDIT_ACTION, DELETE_ACTION);
    }

    private CompositeCell<T> createCompositeCell(String... actionNames) {
      List<HasCell<T, ?>> hasCells = new ArrayList<HasCell<T, ?>>();

      final Cell<String> cell = new ClickableTextCell() {

        @Override
        public void render(String value, Object viewData, StringBuilder sb) {
          super.render(translations.actionMap().get(value), viewData, sb);
        }
      };

      for(final String actionName : actionNames) {
        hasCells.add(new HasCell<T, String>() {

          @Override
          public Cell<String> getCell() {
            return cell;
          }

          @Override
          public FieldUpdater<T, String> getFieldUpdater() {
            return hasCellFieldUpdater;
          }

          @Override
          public String getValue(T object) {
            return actionName;
          }
        });
      }

      return new CompositeCell<T>(hasCells);
    }
  }
}
