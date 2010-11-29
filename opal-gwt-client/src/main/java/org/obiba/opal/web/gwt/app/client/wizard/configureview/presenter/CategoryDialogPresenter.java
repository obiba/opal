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

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.LabelListPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.CategoryUpdateEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.UpdateType;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;

public class CategoryDialogPresenter extends WidgetPresenter<CategoryDialogPresenter.Display> {

  public interface Display extends WidgetDisplay {

    void clear();

    void showDialog();

    void hideDialog();

    void addInputField(LabelListPresenter.Display inputField);

    void removeInputField();

    HasClickHandlers getSaveButton();

    HasClickHandlers getCancelButton();

    void setCategoryNameEditable(boolean editable);

    HasText getCategoryName();

    HasCloseHandlers<DialogBox> getDialog();

    HasText getCaption();
  }

  @Inject
  private LabelListPresenter labelListPresenter;

  private Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

  private Translations translations = GWT.create(Translations.class);

  private ViewDto viewDto;

  private CategoryDto categoryDto;

  private JsArray<CategoryDto> categories;

  private boolean isBound;

  @SuppressWarnings("unchecked")
  @Inject
  public CategoryDialogPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
    validators.add(new RequiredTextValidator(getDisplay().getCategoryName(), "CategoryDialogNameRequired"));
    validators.add(new UniqueCategoryNameValidator(getDisplay().getCategoryName(), "CategoryNameAlreadyExists"));

    categories = (JsArray<CategoryDto>) JsArray.createArray();
  }

  @Override
  protected void onBind() {
    if(!isBound) {
      labelListPresenter.bind();
      getDisplay().addInputField(labelListPresenter.getDisplay());
      addEventHandlers();

      isBound = true;
    }
  }

  @Override
  protected void onUnbind() {
    if(isBound) {
      labelListPresenter.unbind();
      getDisplay().removeInputField();

      // Reset categoryDto to null, otherwise an Edit followed by an Add will look like another Edit.
      setCategoryDto(null);

      isBound = false;
    }
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
    initDisplayComponents();
    getDisplay().showDialog();
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  protected void initDisplayComponents() {
    setTitle();
    labelListPresenter.setAttributeToDisplay("label");

    if(isEdit()) {
      getDisplay().setCategoryNameEditable(false); // don't allow edits of category name
      getDisplay().getCategoryName().setText(categoryDto.getName());
      labelListPresenter.setAttributes(categoryDto.getAttributesArray());
      labelListPresenter.updateFields();
    } else {
      getDisplay().setCategoryNameEditable(true);
      getDisplay().clear();
    }

    validators.add(labelListPresenter.new BaseLanguageTextRequiredValidator("BaseLanguageLabelRequired"));
  }

  private void setTitle() {
    if(isEdit()) {
      getDisplay().getCaption().setText(translations.editCategory());
    } else {
      getDisplay().getCaption().setText(translations.addNewCategory());
    }
  }

  private void addEventHandlers() {
    super.registerHandler(eventBus.addHandler(ViewConfigurationRequiredEvent.getType(), new ViewConfigurationRequiredEventHandler()));

    super.registerHandler(getDisplay().getSaveButton().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        String errorMessageKey = validate();
        if(errorMessageKey != null) {
          eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, errorMessageKey, null));
          return;
        }
        CategoryDto newCategory = getNewCategoryDto();
        if(isEdit()) {
          eventBus.fireEvent(new CategoryUpdateEvent(newCategory, categoryDto, UpdateType.EDIT));
        } else {
          eventBus.fireEvent(new CategoryUpdateEvent(newCategory, null, UpdateType.ADD));
        }
        getDisplay().hideDialog();
      }
    }));

    super.registerHandler(getDisplay().getCancelButton().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        getDisplay().hideDialog();
      }
    }));

    getDisplay().getDialog().addCloseHandler(new CloseHandler<DialogBox>() {
      @Override
      public void onClose(CloseEvent<DialogBox> event) {
        unbind();
      }
    });

  }

  private CategoryDto getNewCategoryDto() {
    CategoryDto categoryDto = CategoryDto.create();
    categoryDto.setName(getDisplay().getCategoryName().getText());
    categoryDto.setIsMissing(false);
    @SuppressWarnings("unchecked")
    JsArray<AttributeDto> attributes = (JsArray<AttributeDto>) JsArray.createArray();
    Map<String, TextBox> labelMap = labelListPresenter.getDisplay().getLanguageLabelMap();
    for(Map.Entry<String, TextBox> entry : labelMap.entrySet()) {
      if(entry.getValue().getValue() != null && !entry.getValue().getValue().equals("")) {
        AttributeDto attribute = AttributeDto.create();
        if(entry.getKey() == null || entry.getKey().equals("")) {
          attribute.clearLocale();
        } else {
          attribute.setLocale(entry.getKey());
        }
        attribute.setName("label");
        attribute.setValue(entry.getValue().getValue());
        attributes.push(attribute);
      }
    }
    categoryDto.setAttributesArray(attributes);
    return categoryDto;
  }

  public void setViewDto(ViewDto viewDto) {
    this.viewDto = viewDto;

    labelListPresenter.setDatasourceName(this.viewDto.getDatasourceName());
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
      for(int categoryIndex = 0; categoryIndex < categories.length(); categoryIndex++) {
        this.categories.push(categories.get(categoryIndex));
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

  class ViewConfigurationRequiredEventHandler implements ViewConfigurationRequiredEvent.Handler {

    @Override
    public void onViewConfigurationRequired(ViewConfigurationRequiredEvent event) {
      CategoryDialogPresenter.this.setViewDto(event.getView());
    }
  }

  public class UniqueCategoryNameValidator extends AbstractFieldValidator {

    private HasText hasText;

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

}
