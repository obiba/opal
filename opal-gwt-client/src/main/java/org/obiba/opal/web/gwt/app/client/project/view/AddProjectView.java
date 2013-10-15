package org.obiba.opal.web.gwt.app.client.project.view;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.project.presenter.AddProjectPresenter;
import org.obiba.opal.web.gwt.app.client.project.presenter.AddProjectUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.validator.ConstrainedModal;
import org.obiba.opal.web.model.client.database.DatabaseDto;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class AddProjectView extends ModalPopupViewWithUiHandlers<AddProjectUiHandlers>
    implements AddProjectPresenter.Display {

  private static final String DATABASE_NONE = "_none";

  interface Binder extends UiBinder<Widget, AddProjectView> {}

  @UiField
  Modal modal;

  @UiField
  ControlGroup nameGroup;

  @UiField
  HasText name;

  @UiField
  HasText title;

  @UiField
  Chooser database;

  @UiField
  HasText description;

  private final Translations translations;

  @Inject
  public AddProjectView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    initWidget(uiBinder.createAndBindUi(this));

    this.translations = translations;
    modal.setTitle(translations.addProject());
    database.setPlaceholderText(translations.selectDatabase());

    ConstrainedModal constrainedModal = new ConstrainedModal(modal);
    constrainedModal.registerWidget("name", translations.nameLabel(), nameGroup);
  }

  @Override
  public void clearErrors() {
    modal.clearAlert();
  }

  @Override
  public HasText getName() {
    return name;
  }

  @Override
  public HasText getTitle() {
    return title;
  }

  @Override
  public HasText getDescription() {
    return description;
  }

  @Override
  public HasText getDatabase() {
    return new HasText() {
      @Override
      public String getText() {
        int selectedIndex = database.getSelectedIndex();
        String selectedDatabase = selectedIndex < 0 ? null : database.getValue(selectedIndex);
        return selectedDatabase == null || DATABASE_NONE.equals(selectedDatabase) ? null : selectedDatabase;
      }

      @Override
      public void setText(@Nullable String text) {
        if(Strings.isNullOrEmpty(text)) return;
        int count = database.getItemCount();
        for(int i = 0; i < count; i++) {
          if(database.getValue(i).equals(text)) {
            database.setSelectedIndex(i);
            break;
          }
        }
      }
    };
  }

  @Override
  public void setAvailableDatabases(JsArray<DatabaseDto> availableDatabases) {
    database.clear();
    database.addItem(translations.none(), DATABASE_NONE);

    String defaultStorageDatabase = DATABASE_NONE;
    for(DatabaseDto databaseDto : JsArrays.toIterable(availableDatabases)) {
      StringBuilder label = new StringBuilder(databaseDto.getName());
      if(databaseDto.getDefaultStorage()) {
        defaultStorageDatabase = databaseDto.getName();
        label.append(" (").append(translations.defaultStorage().toLowerCase()).append(")");
      }
      database.addItem(label.toString(), databaseDto.getName());
    }
    getDatabase().setText(defaultStorageDatabase);
  }

  @Override
  public void showError(@Nullable FormField formField, String message) {
    ControlGroup group = formField == FormField.NAME ? nameGroup : null;
    if(group == null) {
      modal.addAlert(message, AlertType.ERROR);
    } else {
      modal.addAlert(message, AlertType.ERROR, group);
    }
  }

  @UiHandler("save")
  public void onSave(ClickEvent event) {
    getUiHandlers().save();
  }

  @UiHandler("cancel")
  public void onCancel(ClickEvent event) {
    getUiHandlers().cancel();
  }

  @Override
  public void hideDialog() {
    modal.hide();
  }

}
