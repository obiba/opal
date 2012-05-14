/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.configureview.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.navigator.view.CategoriesTable;
import org.obiba.opal.web.gwt.app.client.navigator.view.NavigatorView;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.CategoriesPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;
import org.obiba.opal.web.model.client.magma.CategoryDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 *
 */
public class CategoriesView extends ViewImpl implements CategoriesPresenter.Display {

  @UiTemplate("CategoriesView.ui.xml")
  interface MyUiBinder extends UiBinder<Widget, CategoriesView> {
  }

  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  Button addCategoryButton;

  @UiField(provided = true)
  Table<CategoryDto> categoryTable;

  @UiField
  SimplePager categoryTablePager;

  private final JsArrayDataProvider<CategoryDto> categoryProvider = new JsArrayDataProvider<CategoryDto>();

  private ActionHandler<CategoryDto> editCategoryActionHandler;

  private ActionHandler<CategoryDto> deleteCategoryActionHandler;

  public CategoriesView() {
    categoryTable = new CategoriesTable();
    widget = uiBinder.createAndBindUi(this);
    initCategoryTable();
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  private void initCategoryTable() {

    ActionsColumn<CategoryDto> actionsColumn = new ActionsColumn<CategoryDto>(ActionsColumn.EDIT_ACTION,
        ActionsColumn.DELETE_ACTION);
    actionsColumn.setActionHandler(new ActionHandler<CategoryDto>() {
      @Override
      public void doAction(CategoryDto categoryDto, String actionName) {
        if(ActionsColumn.EDIT_ACTION.equals(actionName)) {
          editCategoryActionHandler.doAction(categoryDto, actionName);
        } else if(ActionsColumn.DELETE_ACTION.equals(actionName)) {
          deleteCategoryActionHandler.doAction(categoryDto, actionName);
        }
      }
    });
    categoryTable.addColumn(actionsColumn, translations.actionsLabel());

    categoryTable.setPageSize(NavigatorView.PAGE_SIZE);
    categoryTablePager.setDisplay(categoryTable);
    categoryProvider.addDataDisplay(categoryTable);
  }

  @Override
  public void renderCategoryRows(JsArray<CategoryDto> categories) {
    categoryProvider.setArray(categories);
    categoryTablePager.firstPage();
    categoryTablePager.setVisible(categoryProvider.getList().size() > categoryTable.getPageSize());
    categoryProvider.refresh();
  }

  @Override
  public HandlerRegistration addAddCategoryHandler(ClickHandler addCategoryHandler) {
    return addCategoryButton.addClickHandler(addCategoryHandler);
  }

  @Override
  public void formEnable(boolean enabled) {
    addCategoryButton.setEnabled(enabled);
  }

  @Override
  public void setEditCategoryActionHandler(ActionHandler<CategoryDto> editCategoryActionHandler) {
    this.editCategoryActionHandler = editCategoryActionHandler;
  }

  @Override
  public void setDeleteCategoryActionHandler(ActionHandler<CategoryDto> deleteCategoryActionHandler) {
    this.deleteCategoryActionHandler = deleteCategoryActionHandler;
  }
}
