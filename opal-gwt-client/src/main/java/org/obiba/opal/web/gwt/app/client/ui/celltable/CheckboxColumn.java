/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui.celltable;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SetSelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.ui.Table;

import java.util.LinkedList;
import java.util.List;

public class CheckboxColumn<T> extends Column<T, Boolean> implements HasActionHandler<Integer> {

  private final Translations translations = GWT.create(Translations.class);

  private final SetSelectionModel<T> selectionModel;

  private final Display<T> display;

  private final boolean singleSelectionModel;

  private ActionHandler<Integer> actionHandler;

  /**
   * Construct a new column with check boxes and multi selection model.
   *
   * @param display
   */
  public CheckboxColumn(Display<T> display) {
    this(display, false);
  }

  /**
   * Construct a new column with check boxes and optional single or multi selection model.
   *
   * @param display
   * @param single
   */
  public CheckboxColumn(final Display<T> display, boolean single) {
    super(new CheckboxCell(true, true) {
      @Override
      public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
        // check if forbidden or has conflict
        super.render(context, value, sb);

      }
    });
    this.display = display;
    singleSelectionModel = single;

    selectionModel = single ? new SingleSelectionModel<T>(new ProvidesKey<T>() {

      @Override
      public Object getKey(T item) {
        return display.getItemKey(item);
      }
    }) : new MultiSelectionModel<T>(new ProvidesKey<T>() {

      @Override
      public Object getKey(T item) {
        return display.getItemKey(item);
      }
    });

    setFieldUpdater(new FieldUpdater<T, Boolean>() {

      @Override
      public void update(int index, T object, Boolean value) {
        selectionModel.setSelected(object, value);
        // hide status message when deselecting an element
        // only redraw when the first checkbox is deselected
        int nbDeselected = 0;
        for(T v : display.getTable().getVisibleItems()) {
          if(!selectionModel.isSelected(v)) {
            nbDeselected++;
          }
        }

        // Redraw table when selecting/deselecting the last/first checkbox
        if(singleSelectionModel || nbDeselected <= 1) {
          display.getTable().redraw();
        }

        updateStatusAlert();
        doAction();
      }
    });

    addHandlers();
  }

  public SetSelectionModel<T> getSelectionModel() {
    return selectionModel;
  }

  private void addHandlers() {
    addClearSelectionHandler();
    addSelectAllHandler();
  }

  private void addClearSelectionHandler() {
    if(display.getClearSelection() == null) return;

    display.getClearSelection().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        for(T tc : selectionModel.getSelectedSet()) {
          selectionModel.setSelected(tc, false);
        }
        display.getTable().redraw();
        display.getClearSelection().setVisible(false);
      }
    });
  }

  private void addSelectAllHandler() {
    if(display.getSelectAll() == null) return;

    // init SelectAll and Clear Selection links
    display.getSelectAll().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        display.selectAllItems(new ItemSelectionHandler<T>() {
          @Override
          public void onItemSelection(T t) {
            selectionModel.setSelected(t, true);
          }
        });

        display.getTable().redraw();
        updateStatusAlert();
        doAction();
      }
    });
  }

  public void clearSelection() {
    selectionModel.clear();
    display.getTable().redraw();
  }

  public void setSelected(T item, boolean selected) {
    selectionModel.setSelected(item, selected);
  }

  /**
   * @return List of items in the same order as they appear on screen
   */
  public List<T> getSelectedItems() {
    List<T> list = new LinkedList<T>();
    list.addAll(selectionModel.getSelectedSet());
    return list;
  }

  @Override
  public Boolean getValue(T object) {
    // Get the value from the selection model.
    return selectionModel.isSelected(object);
  }

  public Header<Boolean> getCheckColumnHeader() {
    Header<Boolean> checkHeader = new Header<Boolean>(new CheckboxCell(true, true) {
      @Override
      public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
        if(getTotalRowCount() == 0) {
          sb.append(SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" disabled=\"disabled\"/>"));
        } else {
          super.render(context, value, sb);
        }

      }
    }) {

      @Override
      public Boolean getValue() {
        updateStatusAlert();
        if(getTotalRowCount() == 0) {
          return false;
        }

        // Value of the header checkbox for the current page
        for(T tc : display.getTable().getVisibleItems()) {
          if(!selectionModel.isSelected(tc)) {
            return false;
          }
        }

        return true;
      }
    };
    checkHeader.setUpdater(new ValueUpdater<Boolean>() {

      @Override
      public void update(Boolean value) {

        if(getTotalRowCount() == 0) return;

        for(T tc : display.getTable().getVisibleItems()) {
          selectionModel.setSelected(tc, value);
        }

        doAction();

        display.getTable().redraw();
      }

    });

    return checkHeader;
  }

  private void updateStatusAlert() {

    if(display.getClearSelection() == null || display.getSelectAll() == null || display.getSelectAllStatus() == null)
      return;

    int selectedSize = selectionModel.getSelectedSet().size();
    int count = getTotalRowCount();
    boolean allSelected = selectedSize == count;

    if(display.getSelectActionsAlert() != null && selectedSize == 0) {
      display.getSelectActionsAlert().setVisible(false);
    } else if(allSelected) {
      updateStatusAlertWhenAllSelected(selectedSize);
    } else if(selectedSize > 0) {
      updateStatusAlertWhenNotAllSelected(selectedSize);
    }

    if(display.getSelectTipsAlert() != null) {
      display.getSelectTipsAlert().setVisible(count > 0 && selectedSize == 0);
    }
  }

  private void updateStatusAlertWhenAllSelected(int currentSelected) {
    List<String> args = Lists.newArrayList();

    args.add(display.getNItemLabel(currentSelected));
    if(currentSelected > 1) {
      display.getSelectAllStatus().setText(TranslationsUtils.replaceArguments(translations.allItemsSelected(), args));
    } else {
      display.getSelectAllStatus().setText(TranslationsUtils.replaceArguments(translations.NItemSelected(), args));
    }

    display.getClearSelection().setVisible(true);
    display.getSelectAll().setVisible(false);
  }

  private void updateStatusAlertWhenNotAllSelected(int currentSelected) {
    List<String> args = Lists.newArrayList();

    args.add(display.getNItemLabel(currentSelected));
    if(currentSelected > 1) {
      display.getSelectAllStatus().setText(TranslationsUtils.replaceArguments(translations.NItemsSelected(), args));
    } else {
      display.getSelectAllStatus().setText(TranslationsUtils.replaceArguments(translations.NItemSelected(), args));
    }
    display.getSelectAll().setVisible(true);

    args.clear();
    args.add(display.getNItemLabel(getTotalRowCount()));
    display.getSelectAll().setText(TranslationsUtils.replaceArguments(translations.selectAllNItems(), args));
    display.getClearSelection().setVisible(true);
  }

  private void doAction() {
    // Count the number of selected items on the current page.
    int nbSelected = selectionModel.getSelectedSet().size();
    int count = getTotalRowCount();

    if(display.getSelectActionsAlert() != null) {
      display.getSelectActionsAlert().setVisible(nbSelected > 0);
    }

    if(display.getSelectTipsAlert() != null) {
      display.getSelectTipsAlert().setVisible(count > 0 && nbSelected == 0);
    }

    if(actionHandler != null) {
      actionHandler.doAction(nbSelected, "SELECT");
    }
  }

  @Override
  public void setActionHandler(ActionHandler<Integer> handler) {
    actionHandler = handler;
  }

  private int getTotalRowCount() {
    return display.getTable().getRowCount();
  }
  
  public interface Display<T> {
    /**
     * @return The displayed table
     */
    Table<T> getTable();

    /**
     * @param item
     * @return The unique key of the item
     */
    Object getItemKey(T item);

    /**
     * @return The link that displays the "clear selection" action
     */
    IconAnchor getClearSelection();

    /**
     * @return The link to select all items
     */
    IconAnchor getSelectAll();

    /**
     * @return The label to show that all items on the page are selected
     */
    HasText getSelectAllStatus();

    /**
     * Get all items and trigger there selection.
     *
     * @param handler
     */
    void selectAllItems(ItemSelectionHandler<T> handler);

    /**
     * @return The type name of item
     */
    String getNItemLabel(int nb);

    /**
     * @return The actions panel after selection was made
     */
    Alert getSelectActionsAlert();

    /**
     * @return The info panel when no selection is made
     */
    Alert getSelectTipsAlert();

  }

  /**
   * Callback on item selection.
   *
   * @param <T>
   */
  public interface ItemSelectionHandler<T> {

    void onItemSelection(T item);

  }

}
