/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.variable;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
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
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.CategoryEditableTable;
import org.obiba.opal.web.gwt.app.client.support.VariableDtos;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.CheckboxColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.EditableTabableColumn;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.opal.LocaleDto;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.obiba.opal.web.gwt.app.client.magma.variable.CategoriesEditorModalPresenter.Display;

public class CategoriesEditorModalView extends ModalPopupViewWithUiHandlers<CategoriesEditorModalUiHandlers>
    implements Display {

  private static final int MIN_HEIGHT = 500;

  private static final int MIN_WIDTH = 700;

  private static final String LABEL = "label";

  private static final int DEFAULT_PAGE_SIZE = 10;

  private final Translations translations;

  private TranslationMessages translationMessages;

  interface Binder extends UiBinder<Widget, CategoriesEditorModalView> {
  }

  private final ListDataProvider<CategoryDto> dataProvider = new ListDataProvider<CategoryDto>();

  private CheckboxColumn<CategoryDto> checkActionCol;

  @UiField
  Modal dialog;

  @UiField
  ControlGroup nameGroup;

  @UiField
  OpalSimplePager pager;

  @UiField
  CategoryEditableTable table;

  @UiField
  TextBox addCategoryName;

  @UiField
  Button closeButton;

  @UiField
  Button saveButton;

  @UiField
  Alert selectAllItemsAlert;

  @UiField
  Alert selectItemTipsAlert;

  @UiField
  Label selectAllStatus;

  @UiField
  IconAnchor selectAllAnchor;

  @UiField
  IconAnchor clearSelectionAnchor;

  @Inject
  public CategoriesEditorModalView(EventBus eventBus, Binder uiBinder, Translations translations,
                                   TranslationMessages translationMessages) {
    super(eventBus);
    this.translations = translations;
    this.translationMessages = translationMessages;
    initWidget(uiBinder.createAndBindUi(this));
    dialog.setTitle(translations.editCategories());
    dialog.setResizable(true);
    dialog.setMinHeight(MIN_HEIGHT);
    dialog.setMinWidth(MIN_WIDTH);

    table.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.DISABLED);
    table.setSelectionModel(new SingleSelectionModel<CategoryDto>());
    table.setEmptyTableWidget(new Label(translations.noCategoriesLabel()));
    table.setPageSize(DEFAULT_PAGE_SIZE);

    pager.setDisplay(table);
    dataProvider.addDataDisplay(table);
  }

  @UiHandler("addButton")
  void onAddButton(ClickEvent event) {
    addCategory();
  }

  @UiHandler("discoverButton")
  void onDiscoverButton(ClickEvent event) {
    getUiHandlers().onDiscoverCategories();
  }

  @UiHandler("addCategoryName")
  void onKeyDown(KeyDownEvent event) {
    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
      addCategory();
      // Prevent the window from reloading
      event.preventDefault();
    }
  }

  @UiHandler("deleteLink")
  void onDelete(ClickEvent event) {
    // Remove selected items from table
    List<CategoryDto> categories = new ArrayList<CategoryDto>();
    for (CategoryDto categoryDto : dataProvider.getList()) {
      if (!checkActionCol.getSelectionModel().isSelected(categoryDto)) {
        categories.add(categoryDto);
      }
    }
    checkActionCol.clearSelection();

    dataProvider.setList(categories);
    dataProvider.refresh();
    pager.setPagerVisible(dataProvider.getList().size() >= DEFAULT_PAGE_SIZE);
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
    for (CategoryDto categoryDto : dataProvider.getList()) {
      if (checkActionCol.getSelectionModel().isSelected(categoryDto)) {
        if (reordered.isEmpty()) {
          pos = i - 1;
        }
        reordered.add(categoryDto);
      } else {
        categories.add(categoryDto);
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
    Collection<CategoryDto> reordered = new ArrayList<CategoryDto>();

    int i = 0;
    int pos = 0;
    for (CategoryDto c : dataProvider.getList()) {
      if (checkActionCol.getSelectionModel().isSelected(c)) {
        if (reordered.isEmpty()) {
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
  public void setInProgress(boolean progress) {
    dialog.setBusy(progress);
    saveButton.setEnabled(!progress);
    closeButton.setEnabled(!progress);
  }

  @Override
  public void renderCategoryRows(JsArray<CategoryDto> rows, List<LocaleDto> locales) {
    addEditableColumns(locales);

    dataProvider.setList(JsArrays.toList(rows));
    dataProvider.refresh();
    pager.setPagerVisible(dataProvider.getList().size() >= DEFAULT_PAGE_SIZE);
  }

  @Override
  public void addCategories(List<String> observedCategories) {
    dialog.clearAlert(nameGroup);
    List<CategoryDto> existingCats = new ArrayList<CategoryDto>(dataProvider.getList());
    List<String> existingCatNames = Lists.newArrayList();
    for (CategoryDto existingCat : existingCats)
      existingCatNames.add(existingCat.getName());

    for (String name : observedCategories) {
      // Validate that the new name does not conflicts with an existing one
      if (!existingCatNames.contains(name)) {
        CategoryDto newCategory = CategoryDto.create();
        newCategory.setName(name);
        newCategory.setIsMissing(false);
        existingCats.add(newCategory);
        existingCatNames.add(name);
      }
    }

    dataProvider.setList(existingCats);
    dataProvider.refresh();
    pager.setPagerVisible(dataProvider.getList().size() >= DEFAULT_PAGE_SIZE);
    pager.setPage(0);
  }

  private void addEditableColumns(Iterable<LocaleDto> locales) {
    checkActionCol = new CheckboxColumn<CategoryDto>(new CategoryDtoDisplay());
    table.addColumn(checkActionCol, checkActionCol.getCheckColumnHeader());
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

    // Render no locale
    renderLocalizedCategoryRows("");
    // prepare cells for each translations
    for (LocaleDto locale : locales) {
      renderLocalizedCategoryRows(locale.getName());
    }
    renderMissingColumn();
  }

  private void renderMissingColumn() {
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

  private void renderLocalizedCategoryRows(final String localeName) {
    Column<CategoryDto, String> labelCol = new EditableTabableColumn<CategoryDto>() {
      @Override
      public String getValue(CategoryDto object) {
        AttributeDto label = VariableDtos.getAttribute(object, LABEL, localeName);
        return label == null ? "" : Strings.nullToEmpty(label.getValue());
      }
    };
    labelCol.setFieldUpdater(new FieldUpdater<CategoryDto, String>() {
      @Override
      public void update(int index, CategoryDto object, String value) {
        AttributeDto label = VariableDtos.getAttribute(object, LABEL, localeName);
        if (label == null) {
          // Create new attribute
          VariableDtos.createAttribute(object, LABEL, localeName, value);
        } else {
          label.setValue(value);
        }
      }
    });

    String headerString = translations.labelLabel();
    if (!localeName.isEmpty()) {
      headerString += " (" + translations.localeMap().get(localeName) + ")";
    }

    table.addColumn(labelCol, headerString);
  }

  @Override
  public JsArray<CategoryDto> getCategories() {
    JsArray<CategoryDto> list = JsArrays.create();

    for (CategoryDto v : dataProvider.getList()) {
      list.push(v);
    }
    return list;
  }

  @Override
  public void showError(String message, @Nullable ControlGroup group) {
    if (group == null) {
      dialog.addAlert(message, AlertType.ERROR);
    } else {
      dialog.addAlert(message, AlertType.ERROR, group);
    }
  }

  private void addCategory() {
    addCategory(addCategoryName.getText());
  }

  private void addCategory(String name) {
    dialog.clearAlert(nameGroup);
    List<CategoryDto> existingCat = new ArrayList<CategoryDto>(dataProvider.getList());

    // Validate that the new name does not conflicts with an existing one
    if (!validateName(name, existingCat)) return;

    CategoryDto newCategory = CategoryDto.create();
    newCategory.setName(name);
    newCategory.setIsMissing(false);
    existingCat.add(newCategory);

    dataProvider.setList(existingCat);
    dataProvider.refresh();
    pager.setPagerVisible(dataProvider.getList().size() >= DEFAULT_PAGE_SIZE);
    pager.setPage(0);
    addCategoryName.setText("");
  }

  private boolean validateName(String name, Iterable<CategoryDto> existingCat) {
    if (name.isEmpty()) {
      showError(translations.categoryNameRequired(), nameGroup);
      return false;
    }
    for (CategoryDto categoryDto : existingCat) {
      if (categoryDto.getName().equals(name)) {
        showError(translations.categoryNameAlreadyExists(), nameGroup);
        return false;
      }
    }
    return true;
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
    public IconAnchor getClearSelection() {
      return clearSelectionAnchor;
    }

    @Override
    public IconAnchor getSelectAll() {
      return selectAllAnchor;
    }

    @Override
    public HasText getSelectAllStatus() {
      return selectAllStatus;
    }

    @Override
    public void selectAllItems(CheckboxColumn.ItemSelectionHandler<CategoryDto> handler) {
      for (CategoryDto item : dataProvider.getList())
        handler.onItemSelection(item);
    }

    @Override
    public String getNItemLabel(int nb) {
      return translationMessages.nCategoriesLabel(nb).toLowerCase();
    }

    @Override
    public Alert getSelectActionsAlert() {
      return selectAllItemsAlert;
    }

    @Override
    public Alert getSelectTipsAlert() {
      return selectItemTipsAlert;
    }
  }
}
