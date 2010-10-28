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
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.LabelListPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.CategoryUpdateEvent;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;

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
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;

public class CategoryDialogPresenter extends WidgetPresenter<CategoryDialogPresenter.Display> {

  public interface Display extends WidgetDisplay {
    void showDialog();

    void hideDialog();

    HasClickHandlers getSaveButton();

    HasClickHandlers getCancelButton();

    HasText getCategoryName();

    HasCloseHandlers<DialogBox> getDialog();

    HasText getCaption();

    ScrollPanel getInputFieldPanel();

  }

  @Inject
  private LabelListPresenter labelListPresenter;

  private Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

  private Translations translations = GWT.create(Translations.class);

  private CategoryDto categoryDto;

  private JsArray<CategoryDto> categories;

  @Inject
  public CategoryDialogPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
    validators.add(new RequiredTextValidator(getDisplay().getCategoryName(), "CategoryNameRequired"));
    validators.add(new UniqueCategoryNameValidator(getDisplay().getCategoryName(), "CategoryNameAlreadyExists"));
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    initDisplayComponents();
    addEventHandlers();
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
    getDisplay().getInputFieldPanel().remove(labelListPresenter.getDisplay().asWidget());
    labelListPresenter.unbind();
  }

  @Override
  public void refreshDisplay() {
    labelListPresenter.refreshDisplay();
  }

  @Override
  public void revealDisplay() {
    getDisplay().showDialog();
    labelListPresenter.revealDisplay();
  }

  protected void initDisplayComponents() {
    labelListPresenter.bind();
    if(categoryDto != null) {
      labelListPresenter.setAttributes(categoryDto.getAttributesArray());
    }
    labelListPresenter.setAttributeToDisplay("label");
    labelListPresenter.bind();
    setTitle();
    populateForm();
    getDisplay().getInputFieldPanel().clear();
    getDisplay().getInputFieldPanel().add(labelListPresenter.getDisplay().asWidget());
  }

  private void setTitle() {
    if(categoryDto == null) {
      getDisplay().getCaption().setText(translations.addNewCategory());
    } else {
      getDisplay().getCaption().setText(translations.editCategory());
    }
  }

  private void populateForm() {
    if(categoryDto != null) {
      getDisplay().getCategoryName().setText(categoryDto.getName());
    }
  }

  private void addEventHandlers() {
    super.registerHandler(getDisplay().getSaveButton().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        String errorMessageKey = validate();
        if(errorMessageKey != null) {
          eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, errorMessageKey, null));
          return;
        }
        CategoryDto newCategory = getNewCategoryDto();
        if(isEdit()) {
          replaceCategory(categoryDto, newCategory);
        } else {
          addCategory(categoryDto);
        }
        eventBus.fireEvent(new CategoryUpdateEvent());
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
    @SuppressWarnings("unchecked")
    JsArray<AttributeDto> attributes = (JsArray<AttributeDto>) JsArray.createArray();
    Map<String, TextBox> labelMap = labelListPresenter.getDisplay().getLanguageLabelMap();
    for(Map.Entry<String, TextBox> entry : labelMap.entrySet()) {
      AttributeDto attribute = AttributeDto.create();
      attribute.setLocale(entry.getKey());
      attribute.setName(entry.getValue().getName());
      attribute.setValue(entry.getValue().getValue());
      attributes.push(attribute);
    }
    categoryDto.setAttributesArray(attributes);
    return categoryDto;
  }

  public void setCategoryDto(CategoryDto categoryDto) {
    this.categoryDto = categoryDto;
  }

  public void setCategories(JsArray<CategoryDto> categories) {
    this.categories = categories;
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

    private HasText hasText;

    public UniqueCategoryNameValidator(HasText hasText, String errorMessageKey) {
      super(errorMessageKey);
      this.hasText = hasText;
    }

    @Override
    protected boolean hasError() {
      if(hasText.getText().equals(categoryDto.getName())) return false; // Edits can have the same name.
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

  private void replaceCategory(CategoryDto originalCategory, CategoryDto newCategory) {
    categories.set(getCategoriesIndex(originalCategory), newCategory);
  }

  private int getCategoriesIndex(CategoryDto category) {
    for(int i = 0; i < categories.length(); i++) {
      if(categories.get(i).equals(category)) return i;
    }
    return -1;
  }

  private void addCategory(CategoryDto categoryDto) {
    categories.push(categoryDto);
  }

}
