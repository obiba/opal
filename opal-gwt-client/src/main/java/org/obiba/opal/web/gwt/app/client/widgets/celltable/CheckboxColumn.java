package org.obiba.opal.web.gwt.app.client.widgets.celltable;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;

import com.github.gwtbootstrap.client.ui.Alert;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;

public class CheckboxColumn<T> extends Column<T, Boolean> {

  private final Translations translations = GWT.create(Translations.class);

  private final MultiSelectionModel<T> selectionModel;

  private final Display<T> display;

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
//    this.table = table;

    selectionModel = new MultiSelectionModel<T>(new ProvidesKey<T>() {

      @Override
      public Object getKey(T item) {
        return display.getItemKey(item);
      }
    });

    setFieldUpdater(new FieldUpdater<T, Boolean>() {
      @SuppressWarnings("unchecked")
      @Override
      public void update(int index, T object, Boolean value) {
        selectionModel.setSelected(object, value);

        //hide status message when deselecting an element
        // only redraw when the first checkbox is deselected
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
        display.getSelectAllWidget().setVisible(false);
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
        updateSelectAllStatus(translations.allItemsSelected(), display.getDataProvider().getList().size());
        display.getSelectAll().setVisible(false);
        display.getClearSelection().setVisible(true);
      }
    });
  }

  @Override
  public Boolean getValue(T object) {
    // Get the value from the selection model.
    return selectionModel.isSelected(object);
  }

  public Header<Boolean> getTableListCheckColumnHeader() {
    Header<Boolean> checkHeader = new Header<Boolean>(new CheckboxCell(true, true)) {

      @Override
      public Boolean getValue() {
        if(display.getDataProvider().getList().isEmpty()) {
          return false;
        }

        for(T tc : display.getTable().getVisibleItems()) {
          if(!selectionModel.isSelected(tc)) {
            // hide status message
            display.getSelectAllWidget().setVisible(false);
            return false;
          }
        }
        display.getSelectAllWidget().setVisible(true);
        return true;
      }
    };
    checkHeader.setUpdater(new ValueUpdater<Boolean>() {

      @Override
      public void update(Boolean value) {
        for(T tc : display.getTable().getVisibleItems()) {
          selectionModel.setSelected(tc, value);
        }
        if(display.getTable().getVisibleItemCount() < display.getDataProvider().getList().size()) {
          displaySelectAllItemsAction(display.getTable().getVisibleItemCount(),
              display.getDataProvider().getList().size(), value);
        }
        display.getTable().redraw();
      }

      private void displaySelectAllItemsAction(int visibleCount, int totalCount, Boolean value) {
        if(value) {
          updateSelectAllStatus(translations.allNItemsSelected(), visibleCount);

          List<String> args = new ArrayList<String>();
          args.add(String.valueOf(totalCount));
          args.add(display.getItemNamePlural());
          display.getSelectAll().setVisible(true);
          display.getSelectAll().setText(TranslationsUtils.replaceArguments(translations.selectAllNItems(), args));
        }
        display.getSelectAllWidget().setVisible(value);

      }
    });
    return checkHeader;
  }

  private void updateSelectAllStatus(String message, int count) {
    List<String> args = new ArrayList<String>();
    args.add(String.valueOf(count));
    args.add(display.getItemNamePlural());
    display.getSelectAllStatus().setText(TranslationsUtils.replaceArguments(message, args));
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
     * @return The alert that will be shown/hide when selecting/deselecting all items
     */
    Alert getSelectAllWidget();

    /**
     * @return The type name of items
     */
    String getItemNamePlural();
  }
}
