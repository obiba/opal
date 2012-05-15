/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter;

import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.CategoriesUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.CategoryUpdateEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.UpdateType;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 *
 */
public class CategoriesPresenter extends PresenterWidget<CategoriesPresenter.Display> {

  private static final Translations translations = GWT.create(Translations.class);

  private static final TranslationMessages translationMessages = GWT.create(TranslationMessages.class);

  @Inject
  private Provider<CategoryDialogPresenter> categoryDialogPresenterProvider;

  private VariableDto currentVariable;

  private ViewDto viewDto;

  @Inject
  public CategoriesPresenter(EventBus eventBus, Display view) {
    super(eventBus, view);
  }

  public void setCurrentVariable(VariableDto currentVariable) {
    this.currentVariable = currentVariable;
  }

  public void setViewDto(ViewDto viewDto) {
    this.viewDto = viewDto;
  }

  @Override
  protected void onBind() {
    super.onBind();
    registerEventHandlers();
  }

  public void renderCategories() {
    getView().renderCategoryRows(currentVariable.getCategoriesArray());
  }

  private void registerEventHandlers() {
    // show dialog
    registerHandler(getView().addAddCategoryHandler(new AddCategoryHandler()));
    getView().setEditCategoryActionHandler(new EditCategoryHandler());

    DeleteCategoryActionHandler deleteCategoryActionHandler = new DeleteCategoryActionHandler();
    getView().setDeleteCategoryActionHandler(deleteCategoryActionHandler);
    registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), deleteCategoryActionHandler));

    // execute category update
    registerHandler(getEventBus().addHandler(CategoryUpdateEvent.getType(), new CategoryUpdateEventHandler()));
  }

  private void prepareCategoryDialog(CategoryDto categoryDto) {
    CategoryDialogPresenter categoryDialogPresenter = categoryDialogPresenterProvider.get();
    categoryDialogPresenter.bind();
    categoryDialogPresenter.setViewDto(viewDto);
    categoryDialogPresenter.setCategoryDto(categoryDto);
    categoryDialogPresenter.setCategories(currentVariable.getCategoriesArray());
    categoryDialogPresenter.revealDisplay();
  }

  private class AddCategoryHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      prepareCategoryDialog(null);
    }
  }

  private class EditCategoryHandler implements ActionHandler<CategoryDto> {

    @Override
    public void doAction(CategoryDto categoryDto, String actionName) {
      if(ActionsColumn.EDIT_ACTION.equals(actionName)) {
        prepareCategoryDialog(categoryDto);
      }
    }
  }

  class DeleteCategoryActionHandler implements ActionHandler<CategoryDto>, ConfirmationEvent.Handler {

    private Runnable runDelete;

    @Override
    public void doAction(final CategoryDto deletedCategory, String actionName) {
      if(!ActionsColumn.DELETE_ACTION.equals(actionName)) return;
      runDelete = new Runnable() {

        @Override
        public void run() {
          fireEvent(new CategoryUpdateEvent(deletedCategory, UpdateType.DELETE));
        }

      };
      fireEvent(ConfirmationRequiredEvent.createWithMessages(runDelete, translations.deleteCategory(),
          translationMessages.confirmDeleteCategory(deletedCategory.getName())));
    }

    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if(event.isConfirmed() && event.getSource() == runDelete) {
        runDelete.run();
      }
    }
  }

  class CategoryUpdateEventHandler implements CategoryUpdateEvent.Handler {

    @Override
    public void onCategoryUpdate(CategoryUpdateEvent event) {
      switch(event.getUpdateType()) {
        case ADD:
          addCategory(event);
          break;
        case EDIT:
          replaceCategory(event);
          break;
        case DELETE:
          deleteCategory(event.getCategory());
          break;
      }
      getView().renderCategoryRows(currentVariable.getCategoriesArray());
      fireEvent(new CategoriesUpdatedEvent());
    }

    @SuppressWarnings("unchecked")
    private void addCategory(CategoryUpdateEvent event) {
      if(currentVariable.getCategoriesArray() == null) {
        currentVariable.setCategoriesArray((JsArray<CategoryDto>) JsArray.createArray());
      }
      currentVariable.getCategoriesArray().push(event.getCategory());
    }

    private void replaceCategory(CategoryUpdateEvent event) {
      for(int i = 0; i < currentVariable.getCategoriesArray().length(); i++) {
        CategoryDto category = currentVariable.getCategoriesArray().get(i);
        if(category.getName().equals(event.getOriginalCategory().getName())) {
          currentVariable.getCategoriesArray().set(i, event.getCategory());
          break;
        }
      }
    }

    private void deleteCategory(CategoryDto categoryToDelete) {
      @SuppressWarnings("unchecked")
      JsArray<CategoryDto> result = (JsArray<CategoryDto>) JsArray.createArray();
      for(int i = 0; i < currentVariable.getCategoriesArray().length(); i++) {
        CategoryDto category = currentVariable.getCategoriesArray().get(i);
        if(!category.getName().equals(categoryToDelete.getName())) {
          result.push(category);
        }
      }
      currentVariable.setCategoriesArray(result);
    }

  }

  public interface Display extends View {

    void formEnable(boolean enabled);

    HandlerRegistration addAddCategoryHandler(ClickHandler addCategoryHandler);

    void renderCategoryRows(JsArray<CategoryDto> categories);

    void setEditCategoryActionHandler(ActionHandler<CategoryDto> editCategoryActionHandler);

    void setDeleteCategoryActionHandler(ActionHandler<CategoryDto> deleteCategoryActionHandler);
  }
}
