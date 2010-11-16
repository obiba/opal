/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.CategoryUpdateEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.UpdateType;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.inject.Inject;

/**
 *
 */
public class CategoriesPresenter extends LocalizablesPresenter {
  //
  // Instance Variables
  //

  @Inject
  private CategoryDialogPresenter categoryDialogPresenter;

  private ClickHandler addButtonClickHandler;

  private EditActionHandler editActionHandler;

  private DeleteActionHandler deleteActionHandler;

  //
  // Constructors
  //

  @Inject
  public CategoriesPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // LocalizablesPresenter Methods
  //

  @Override
  protected List<Localizable> getLocalizables(String localeName) {
    List<Localizable> localizables = new ArrayList<Localizable>();

    variableDto.setCategoriesArray(JsArrays.toSafeArray(variableDto.getCategoriesArray()));

    for(int categoryIndex = 0; categoryIndex < variableDto.getCategoriesArray().length(); categoryIndex++) {
      final CategoryDto categoryDto = variableDto.getCategoriesArray().get(categoryIndex);
      categoryDto.setAttributesArray(JsArrays.toSafeArray(categoryDto.getAttributesArray()));

      for(int attributeIndex = 0; attributeIndex < categoryDto.getAttributesArray().length(); attributeIndex++) {
        final AttributeDto attributeDto = categoryDto.getAttributesArray().get(attributeIndex);

        if(attributeDto.getName().equals("label") && attributeDto.getLocale().equals(localeName)) {
          localizables.add(new Localizable() {

            @Override
            public String getName() {
              return categoryDto.getName();
            }

            @Override
            public String getLabel() {
              return attributeDto.getValue();
            }
          });

          break;
        }
      }
    }

    return localizables;
  }

  @Override
  protected void bindDependencies() {
    categoryDialogPresenter.bind();
  }

  @Override
  protected void unbindDependencies() {
    categoryDialogPresenter.unbind();
  }

  @Override
  protected void refreshDependencies() {
    categoryDialogPresenter.refreshDisplay();
  }

  @Override
  protected void addEventHandlers() {
    super.addEventHandlers(); // call superclass method to register common handlers
    super.registerHandler(eventBus.addHandler(CategoryUpdateEvent.getType(), new CategoryUpdateEventHandler()));
  }

  @Override
  protected ClickHandler getAddButtonClickHandler() {
    if(addButtonClickHandler == null) {
      addButtonClickHandler = new AddNewCategoryButtonHandler();
    }
    return addButtonClickHandler;
  }

  @Override
  protected EditActionHandler getEditActionHandler() {
    if(editActionHandler == null) {
      editActionHandler = new EditCategoryHandler();
    }
    return editActionHandler;
  }

  @Override
  protected DeleteActionHandler getDeleteActionHandler() {
    if(deleteActionHandler == null) {
      deleteActionHandler = new DeleteCategoryHandler();
    }
    return deleteActionHandler;
  }

  protected String getDeleteConfirmationTitle() {
    return "deleteCategory";
  }

  protected String getDeleteConfirmationMessage() {
    return "confirmDeleteCategory";
  }

  //
  // Methods
  //

  public void setVariableDto(VariableDto variableDto) {
    this.variableDto = variableDto;
  }

  //
  // Inner Classes / Interfaces
  //

  class AddNewCategoryButtonHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      // Each time the dialog is closed (hidden), it is unbound. So we need to rebind it each time we display it.
      categoryDialogPresenter.bind();
      categoryDialogPresenter.revealDisplay();
    }
  }

  class EditCategoryHandler implements EditActionHandler {

    @Override
    public void onEdit(Localizable localizable) {
      categoryDialogPresenter.setCategoryDto(findCategoryDto(localizable.getName()));

      // Each time the dialog is closed (hidden), it is unbound. So we need to rebind it each time we display it.
      categoryDialogPresenter.bind();

      categoryDialogPresenter.revealDisplay();
    }

    private CategoryDto findCategoryDto(String name) {
      for(int categoryIndex = 0; categoryIndex < variableDto.getCategoriesArray().length(); categoryIndex++) {
        CategoryDto categoryDto = variableDto.getCategoriesArray().get(categoryIndex);
        if(categoryDto.getName().equals(name)) {
          return categoryDto;
        }
      }
      return null;
    }
  }

  class DeleteCategoryHandler implements DeleteActionHandler {

    @SuppressWarnings("unchecked")
    @Override
    public void onDelete(Localizable localizable) {
      JsArray<CategoryDto> updatedCategoriesArray = (JsArray<CategoryDto>) JsArray.createArray();

      for(int categoryIndex = 0; categoryIndex < variableDto.getCategoriesArray().length(); categoryIndex++) {
        CategoryDto categoryDto = variableDto.getCategoriesArray().get(categoryIndex);

        if(!categoryDto.getName().equals(localizable.getName())) {
          updatedCategoriesArray.push(categoryDto);
        }
      }

      variableDto.clearCategoriesArray();
      variableDto.setCategoriesArray(updatedCategoriesArray);
    }
  }

  class CategoryUpdateEventHandler implements CategoryUpdateEvent.Handler {

    @Override
    public void onCategoryUpdate(CategoryUpdateEvent event) {
      if(event.getUpdateType().equals(UpdateType.ADD)) {
        addCategory(event);
      } else if(event.getUpdateType().equals(UpdateType.EDIT)) {
        replaceCategory(event);
      }
      CategoriesPresenter.this.refreshTableData();
    }

    @SuppressWarnings("unchecked")
    private void addCategory(CategoryUpdateEvent event) {
      if(variableDto.getCategoriesArray() == null) {
        variableDto.setCategoriesArray((JsArray<CategoryDto>) JsArray.createArray());
      }

      variableDto.getCategoriesArray().push(event.getNewCategory());
    }

    private void replaceCategory(CategoryUpdateEvent event) {
      for(int categoryIndex = 0; categoryIndex < variableDto.getCategoriesArray().length(); categoryIndex++) {
        CategoryDto categoryDto = variableDto.getCategoriesArray().get(categoryIndex);
        if(categoryDto.getName().equals(event.getOriginalCategory().getName())) {
          variableDto.getCategoriesArray().set(categoryIndex, event.getNewCategory());
          break;
        }
      }
    }
  }
}
