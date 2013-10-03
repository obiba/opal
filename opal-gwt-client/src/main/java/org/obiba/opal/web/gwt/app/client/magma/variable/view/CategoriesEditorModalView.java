/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.variable.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.CategoriesEditorModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.magma.view.CategoryEditableTable;
import org.obiba.opal.web.gwt.app.client.support.VariableDtos;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.CheckboxColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.EditableTabableColumn;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.opal.LocaleDto;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import static org.obiba.opal.web.gwt.app.client.magma.variable.presenter.CategoriesEditorModalPresenter.Display;

public class CategoriesEditorModalView extends ModalPopupViewWithUiHandlers<CategoriesEditorModalUiHandlers>
    implements Display {

  private static final int MIN_WIDTH = 780;

  private static final int MIN_HEIGHT = 700;

  private static final String LABEL = "label";

  private final Widget widget;

  private final Translations translations = GWT.create(Translations.class);

  interface CategoriesEditorModalUiBinder extends UiBinder<Widget, CategoriesEditorModalView> {}

  private static final CategoriesEditorModalUiBinder uiBinder = GWT.create(CategoriesEditorModalUiBinder.class);

  private final ListDataProvider<CategoryDto> dataProvider = new ListDataProvider<CategoryDto>();

  private CheckboxColumn<CategoryDto> checkActionCol;

  @UiField
  Modal dialog;

  @UiField
  Heading variableName;

  @UiField
  ControlGroup nameGroup;

  @UiField
  CategoryEditableTable table;

  @UiField
  TextBox addCategoryName;

  @UiField
  Button addButton;

  @UiField
  Button closeButton;

  @UiField
  Button saveButton;

  @UiField
  Alert selectAllItemsAlert;

  @UiField
  Label selectAllStatus;

  @UiField
  Anchor selectAllAnchor;

  @UiField
  Anchor clearSelectionAnchor;

  @UiField
  Anchor deleteLink;

  @UiField
  Anchor moveUpLink;

  @UiField
  Anchor moveDownLink;

  @Inject
  public CategoriesEditorModalView(EventBus eventBus) {
    super(eventBus);
    widget = uiBinder.createAndBindUi(this);
    dialog.setTitle(translations.editCategories());
    dialog.setResizable(true);
    dialog.setMinWidth(MIN_WIDTH);
    dialog.setMinHeight(MIN_HEIGHT);

    table.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.DISABLED);
    table.setSelectionModel(new SingleSelectionModel<CategoryDto>());
    table.setEmptyTableWidget(new Label(translations.noCategoriesLabel()));

    dataProvider.addDataDisplay(table);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @UiHandler("addButton")
  void onAddButton(ClickEvent event) {
    addCategory();
  }

  @UiHandler("addCategoryName")
  void onKeyDown(KeyDownEvent event) {
    if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
      addCategory();
      // Prevent the window from reloading
      event.preventDefault();
    }
  }

  @UiHandler("deleteLink")
  void onDelete(ClickEvent event) {
    // Remove selected items from table
    List<CategoryDto> categories = new ArrayList<CategoryDto>();
    for(CategoryDto c : dataProvider.getList()) {
      if(!checkActionCol.getSelectionModel().isSelected(c)) {
        categories.add(c);
      }
    }

    dataProvider.setList(categories);
    dataProvider.refresh();
  }

  @UiHandler("closeButton")
  void onClose(ClickEvent event) {
    dialog.hide();
  }

  @UiHandler("saveButton")
  void onSave(ClickEvent event) {
    dialog.clearAlert(nameGroup);
    getUiHandlers().onSave();
  }

  @UiHandler("moveUpLink")
  void onMoveUp(ClickEvent event) {
    List<CategoryDto> categories = new ArrayList<CategoryDto>();
    Collection<CategoryDto> reordered = new ArrayList<CategoryDto>();

    int i = 0;
    int pos = 0;
    for(CategoryDto c : dataProvider.getList()) {
      if(checkActionCol.getSelectionModel().isSelected(c)) {
        if(reordered.isEmpty()) {
          pos = i - 1;
        }
        reordered.add(c);
      } else {
        categories.add(c);
      }
      i++;
    }
    categories.addAll(pos >= 0 ? pos : 0, reordered);

    dataProvider.setList(categories);
    dataProvider.refresh();
  }

  @UiHandler("moveDownLink")
  void onMoveDown(ClickEvent event) {
    List<CategoryDto> categories = new ArrayList<CategoryDto>();
    List<CategoryDto> reordered = new ArrayList<CategoryDto>();

    int i = 0;
    int pos = 0;
    for(CategoryDto c : dataProvider.getList()) {
      if(checkActionCol.getSelectionModel().isSelected(c)) {
        if(reordered.isEmpty()) {
          pos = i + 1;
        }
        reordered.add(c);
      } else {
        categories.add(c);
        i++;
      }
    }
    categories.addAll(pos, reordered);

    dataProvider.setList(categories);
    dataProvider.refresh();
  }

  @Override
  public void renderCategoryRows(JsArray<CategoryDto> rows, List<LocaleDto> locales) {
    addEditableColumns(locales);

    dataProvider.setList(JsArrays.toList(JsArrays.toSafeArray(rows)));
    dataProvider.refresh();
  }

  private void addEditableColumns(List<LocaleDto> locales) {
    checkActionCol = new CheckboxColumn<CategoryDto>(new CategoryDtoDisplay());
    table.addColumn(checkActionCol, checkActionCol.getTableListCheckColumnHeader());
    table.setColumnWidth(checkActionCol, 1, Style.Unit.PX);

    Column<CategoryDto, String> nameCol = new EditableTabableColumn<CategoryDto>() {
      @Override
      public String getValue(CategoryDto object) {
        return object.getName();
      }
    };
    nameCol.setFieldUpdater(new FieldUpdater<CategoryDto, String>() {
      @Override
      public void update(int index, CategoryDto object, String value) {
        object.setName(value);
      }
    });
    table.addColumn(nameCol, translations.nameLabel());

    // prepare cells for each translations
    for(final LocaleDto locale : locales) {
      Column<CategoryDto, String> labelCol = new EditableTabableColumn<CategoryDto>() {
        @Override
        public String getValue(CategoryDto object) {
          AttributeDto label = VariableDtos.getAttribute(object, LABEL, locale.getName());
          return label == null ? "" : label.getValue();
        }
      };
      labelCol.setFieldUpdater(new FieldUpdater<CategoryDto, String>() {
        @Override
        public void update(int index, CategoryDto object, String value) {
          AttributeDto label = VariableDtos.getAttribute(object, LABEL, locale.getName());
          if(label == null) {
            // Create new attribute
            VariableDtos.createAttribute(object, LABEL, locale.getName(), value);
          } else {
            label.setValue(value);
          }
        }
      });
      table
          .addColumn(labelCol, translations.labelLabel() + " (" + translations.localeMap().get(locale.getName()) + ")");

    }

    Column<CategoryDto, Boolean> missingCol = new Column<CategoryDto, Boolean>(new CheckboxCell(true, false)) {
      @Override
      public Boolean getValue(CategoryDto object) {
        // Get the value from the selection model.
        return object.getIsMissing();
      }
    };
    missingCol.setFieldUpdater(new FieldUpdater<CategoryDto, Boolean>() {
      @Override
      public void update(int index, CategoryDto object, Boolean value) {
        object.setIsMissing(value);
      }
    });
    table.addColumn(missingCol, translations.missingLabel());
  }

  @Override
  public JsArray<CategoryDto> getCategories() {
    JsArray<CategoryDto> list = JsArrays.create();

    for(CategoryDto v : dataProvider.getList()) {
      list.push(v);
    }
    return list;
  }

  @Override
  public void showError(String message, @Nullable ControlGroup group) {
    if(group == null) {
      dialog.addAlert(message, AlertType.ERROR);
    } else {
      dialog.addAlert(message, AlertType.ERROR, group);
    }
  }

  private void addCategory() {
    dialog.clearAlert(nameGroup);
    List<CategoryDto> current = new ArrayList<CategoryDto>(dataProvider.getList());

    // Validate that the new name does not conflicts with an existing one
    if(addCategoryName.getText().isEmpty()) {
      showError(translations.categoryNameRequired(), nameGroup);
      return;
    }

    for(CategoryDto c : current) {
      if(c.getName().equalsIgnoreCase(addCategoryName.getText())) {
        showError(translations.categoryNameAlreadyExists(), nameGroup);
        return;
      }
    }

    CategoryDto newCategory = CategoryDto.create();
    newCategory.setName(addCategoryName.getText());
    newCategory.setIsMissing(false);
    current.add(newCategory);

    dataProvider.setList(current);
    dataProvider.refresh();
    addCategoryName.setText("");
  }

  @Override
  public void setVariableName(String name) {
    variableName.setText(name);
  }

  private class CategoryDtoDisplay implements CheckboxColumn.Display<CategoryDto> {

    @Override
    public Table<CategoryDto> getTable() {
      return table;
    }

    @Override
    public Object getItemKey(CategoryDto item) {
      return item.getName();
    }

    @Override
    public Anchor getClearSelection() {
      return clearSelectionAnchor;
    }

    @Override
    public Anchor getSelectAll() {
      return selectAllAnchor;
    }

    @Override
    public HasText getSelectAllStatus() {
      return selectAllStatus;
    }

    @Override
    public ListDataProvider<CategoryDto> getDataProvider() {
      return dataProvider;
    }

    @Override
    public String getItemNamePlural() {
      return translations.categoriesLabel().toLowerCase();
    }

    @Override
    public String getItemNameSingular() {
      return translations.categoryLabel().toLowerCase();
    }

    @Override
    public Alert getAlert() {
      return selectAllItemsAlert;
    }
  }
}
