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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.util.AttributeDtos;
import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.LabelListPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.CategoryUpdateEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.UpdateType;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class CategoryDialogPresenter extends PresenterWidget<CategoryDialogPresenter.Display> {

  private static final Translations translations = GWT.create(Translations.class);

  private LabelListPresenter labelListPresenter;

  private final Collection<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

  private CategoryDto categoryDto;

  private JsArray<CategoryDto> categories;

  @SuppressWarnings("unchecked")
  @Inject
  public CategoryDialogPresenter(EventBus eventBus, Display display, LabelListPresenter labelListPresenter) {
    super(eventBus, display);
    this.labelListPresenter = labelListPresenter;
    categories = (JsArray<CategoryDto>) JsArray.createArray();
  }

  @Override
  protected void onBind() {
    labelListPresenter.bind();
    getView().addInputField(labelListPresenter.getView());
    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
    labelListPresenter.unbind();
    getView().removeInputField();

    // Reset categoryDto to null, otherwise an Edit followed by an Add will look like another Edit.
    setCategoryDto(null);

    validators.clear();
  }

  @Override
  public void onReveal() {
    initDisplayComponents();
    addValidators();
    getView().showDialog();
  }

  private void addValidators() {
    validators.add(new RequiredTextValidator(getView().getCategoryName(), translations.categoryDialogNameRequired()));
    validators
        .add(new UniqueCategoryNameValidator(getView().getCategoryName(), translations.categoryNameAlreadyExists()));
    validators.add(labelListPresenter.new BaseLanguageTextRequiredValidator(translations.categoryLabelRequired()));
  }

  protected void initDisplayComponents() {
    setTitle();
    labelListPresenter.setAttributeToDisplay(null, AttributeDtos.LABEL_ATTRIBUTE);

    if(isEdit()) {
      getView().getCategoryName().setText(categoryDto.getName());
      getView().getMissing().setValue(categoryDto.getIsMissing());
      labelListPresenter.setAttributes(categoryDto.getAttributesArray());
      labelListPresenter.updateFields();
    } else {
      getView().clear();
    }
  }

  private void setTitle() {
    getView().getCaption().setText(isEdit() ? translations.editCategory() : translations.addNewCategory());
  }

  private void addEventHandlers() {
    registerHandler(getView().getSaveButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        String errorMessageKey = validate();
        if(errorMessageKey != null) {
          getEventBus().fireEvent(NotificationEvent.newBuilder().error(errorMessageKey).build());
          return;
        }
        CategoryDto newCategory = getNewCategoryDto();
        if(isEdit()) {
          getEventBus().fireEvent(new CategoryUpdateEvent(newCategory, categoryDto, UpdateType.EDIT));
        } else {
          getEventBus().fireEvent(new CategoryUpdateEvent(newCategory, null, UpdateType.ADD));
        }
        getView().hideDialog();
      }

      private CategoryDto getNewCategoryDto() {
        CategoryDto newCategory = CategoryDto.create();
        newCategory.setName(getView().getCategoryName().getText());
        newCategory.setIsMissing(getView().getMissing().getValue());
        @SuppressWarnings("unchecked")
        JsArray<AttributeDto> attributes = (JsArray<AttributeDto>) JsArray.createArray();
        Map<String, TextBoxBase> labelMap = labelListPresenter.getView().getLanguageLabelMap();
        for(Map.Entry<String, TextBoxBase> entry : labelMap.entrySet()) {
          String value = entry.getValue().getValue();
          if(!Strings.isNullOrEmpty(value)) {
            String locale = entry.getKey();
            attributes.push(AttributeDtos.create(value, locale));
          }
        }
        newCategory.setAttributesArray(attributes);
        return newCategory;
      }
    }));

    registerHandler(getView().getCancelButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getView().hideDialog();
      }
    }));

    registerHandler(getView().getDialog().addCloseHandler(new CloseHandler<DialogBox>() {
      @Override
      public void onClose(CloseEvent<DialogBox> event) {
        unbind();
      }
    }));

  }

  public void setViewDto(ViewDto viewDto) {
    labelListPresenter.setDatasourceName(viewDto.getDatasourceName());
  }

  public void setCategoryDto(CategoryDto categoryDto) {
    this.categoryDto = categoryDto;

    if(categoryDto != null) {
      categoryDto.setAttributesArray(JsArrays.toSafeArray(categoryDto.getAttributesArray()));
    }
  }

  @SuppressWarnings("unchecked")
  public void setCategories(JsArray<CategoryDto> categories) {
    this.categories = (JsArray<CategoryDto>) JsArray.createArray();

    if(categories != null) {
      for(int i = 0; i < categories.length(); i++) {
        this.categories.push(categories.get(i));
      }
    }
  }

  String validate() {
    for(FieldValidator validator : validators) {
      String errorMessageKey = validator.validate();
      if(errorMessageKey != null) {
        return errorMessageKey;
      }
    }
    return null;
  }

  public class UniqueCategoryNameValidator extends AbstractFieldValidator {

    private final HasText hasText;

    public UniqueCategoryNameValidator(HasText hasText, String errorMessageKey) {
      super(errorMessageKey);
      this.hasText = hasText;
    }

    @Override
    protected boolean hasError() {
      if(isEdit()) {
        if(hasText.getText().equals(categoryDto.getName())) return false; // Edits can have the same name.
      }

      for(int i = 0; i < categories.length(); i++) {
        CategoryDto dto = categories.get(i);
        // Using the same name as an existing category is not permitted.
        if(hasText.getText().equals(dto.getName())) return true;
      }
      return false;
    }

  }

  private boolean isEdit() {
    return categoryDto != null;
  }

  public interface Display extends View {

    void clear();

    void showDialog();

    void hideDialog();

    void addInputField(LabelListPresenter.Display inputField);

    void removeInputField();

    HasClickHandlers getSaveButton();

    HasClickHandlers getCancelButton();

    HasText getCategoryName();

    HasValue<Boolean> getMissing();

    HasCloseHandlers<DialogBox> getDialog();

    HasText getCaption();
  }

}
