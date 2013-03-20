/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.widgets.celltable;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;

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
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;

public class CheckboxColumn<T> extends Column<T, Boolean> implements HasActionHandler<Integer> {

  private final Translations translations = GWT.create(Translations.class);

  private final MultiSelectionModel<T> selectionModel;

  private final Display<T> display;

  private ActionHandler<Integer> actionHandler;

  /**
   * Construct a new Column with a given {@link com.google.gwt.cell.client.Cell}.
   *
   * @param cell the Cell used by this Column
   */
  public CheckboxColumn(final Display<T> display) {
    super(new CheckboxCell(true, true) {
      @Override
      public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
        // check if forbidden or has conflict
        super.render(context, value, sb);
      }
    });
    this.display = display;

    selectionModel = new MultiSelectionModel<T>(new ProvidesKey<T>() {

      @Override
      public Object getKey(T item) {
        return display.getItemKey(item);
      }
    });

    setFieldUpdater(new FieldUpdater<T, Boolean>() {

      @Override
      public void update(int index, T object, Boolean value) {
        selectionModel.setSelected(object, value);

//        //hide status message when deselecting an element
//        // only redraw when the first checkbox is deselected
        int nbDeselected = 0;
        for(T v : display.getTable().getVisibleItems()) {
          if(!selectionModel.isSelected(v)) {
            nbDeselected++;
          }
        }

        // Redraw table when selecting/deselecting the last/first checkbox
        if(nbDeselected <= 1) {
          display.getTable().redraw();
        }

        updateStatusAlert();
        doAction();

      }
    });

    addHandlers();
  }

  private void addHandlers() {
    display.getClearSelection().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        for(T tc : display.getDataProvider().getList()) {
          selectionModel.setSelected(tc, false);
        }
        display.getTable().redraw();
        display.getClearSelection().setVisible(false);
      }
    });

    // init SelectAll and Clear Selection links
    display.getSelectAll().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        for(T tc : display.getDataProvider().getList()) {
          selectionModel.setSelected(tc, true);
        }

        display.getTable().redraw();
        updateStatusAlert();
        doAction();
      }
    });
  }

  public MultiSelectionModel<T> getSelectionModel() {
    return selectionModel;
  }

  @Override
  public Boolean getValue(T object) {
    // Get the value from the selection model.
    return selectionModel.isSelected(object);
  }

  public Header<Boolean> getTableListCheckColumnHeader() {
    Header<Boolean> checkHeader = new Header<Boolean>(new CheckboxCell(true, true) {
      @Override
      public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
        if(display.getDataProvider().getList().isEmpty()) {
          sb.append(SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" disabled=\"disabled\"/>"));
        } else {
          super.render(context, value, sb);
        }

      }
    }) {

      @Override
      public Boolean getValue() {
        doAction();

        updateStatusAlert();
        if(display.getDataProvider().getList().isEmpty()) {
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

        if(display.getDataProvider().getList().isEmpty()) return;

        for(T tc : display.getTable().getVisibleItems()) {
          selectionModel.setSelected(tc, value);
        }

        doAction();

        display.getTable().redraw();
      }

    });

    return checkHeader;
  }

  @SuppressWarnings("OverlyLongMethod")
  private void updateStatusAlert() {

    int currentSelected = 0;
    for(int i = 0; i < display.getTable().getVisibleItemCount(); i++) {
      if(selectionModel.isSelected(display.getTable().getVisibleItem(i))) {
        currentSelected++;
      }
    }

    int selectedSize = selectionModel.getSelectedSet().size();
    boolean allSelected = selectedSize == display.getDataProvider().getList().size();
    boolean allPageSelected = currentSelected == display.getTable().getVisibleItemCount();

    if(allSelected) {
      List<String> args = new ArrayList<String>();
      args.add(String.valueOf(display.getDataProvider().getList().size()));

      if(currentSelected > 1) {
        args.add(display.getItemNamePlural());
        display.getSelectAllStatus().setText(TranslationsUtils.replaceArguments(translations.allItemsSelected(), args));
      } else {
        args.add(display.getItemNameSingular());
        display.getSelectAllStatus().setText(TranslationsUtils.replaceArguments(translations.NItemSelected(), args));
      }

      display.getClearSelection().setVisible(true);
      display.getSelectAll().setVisible(false);
    } else if(allPageSelected) {
      List<String> args = new ArrayList<String>();
      args.add(String.valueOf(currentSelected));

      if(currentSelected > 1) {
        args.add(display.getItemNamePlural());
        display.getSelectAllStatus()
            .setText(TranslationsUtils.replaceArguments(translations.allNItemsSelected(), args));
      } else {
        args.add(display.getItemNameSingular());
        display.getSelectAllStatus().setText(TranslationsUtils.replaceArguments(translations.NItemSelected(), args));
      }
      display.getSelectAll().setVisible(true);

      args.clear();
      args.add(String.valueOf(display.getDataProvider().getList().size()));
      args.add(display.getItemNamePlural());
      display.getSelectAll().setText(TranslationsUtils.replaceArguments(translations.selectAllNItems(), args));
      display.getClearSelection().setVisible(false);
    } else {
      List<String> args = new ArrayList<String>();
      args.add(String.valueOf(currentSelected));

      if(currentSelected > 1) {
        args.add(display.getItemNamePlural());
        display.getSelectAllStatus().setText(TranslationsUtils.replaceArguments(translations.NItemsSelected(), args));
      } else {
        args.add(display.getItemNameSingular());
        display.getSelectAllStatus().setText(TranslationsUtils.replaceArguments(translations.NItemSelected(), args));
      }
      display.getSelectAll().setVisible(true);

      args.clear();
      args.add(String.valueOf(display.getDataProvider().getList().size()));
      args.add(display.getItemNamePlural());
      display.getSelectAll().setText(TranslationsUtils.replaceArguments(translations.selectAllNItems(), args));
      display.getClearSelection().setVisible(false);
    }
  }

  private void doAction() {
    // Count the number of selected items on the current page.
    Integer nbSelected = 0;
    for(int i = 0; i < display.getTable().getVisibleItemCount(); i++) {
      if(selectionModel.isSelected(display.getTable().getVisibleItem(i))) {
        nbSelected++;
      }
    }

    if(actionHandler != null) {
      actionHandler.doAction(nbSelected, "SELECT");
    }
  }

  @Override
  public void setActionHandler(ActionHandler<Integer> handler) {
    actionHandler = handler;
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
    Anchor getClearSelection();

    /**
     * @return The link to select all items
     */
    Anchor getSelectAll();

    /**
     * @return The label to show that all items on the page are selected
     */
    HasText getSelectAllStatus();

    /**
     * @return The table data provider
     */
    ListDataProvider<T> getDataProvider();

    /**
     * @return The type name of items
     */
    String getItemNamePlural();

    /**
     * @return The type name of item
     */
    String getItemNameSingular();

//    ClickHandler getSelectClickHandler();
  }
}
