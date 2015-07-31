/*******************************************************************************
 * Copyright 2012(c) OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.database.edit.mongo;

import java.util.Collection;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.administration.database.edit.AbstractDatabaseModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.edit.DatabaseUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.CollapsiblePanel;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.validator.ConstrainedModal;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.PasswordTextBox;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import static org.obiba.opal.web.gwt.app.client.administration.database.edit.AbstractDatabaseModalPresenter.Mode.CREATE;
import static org.obiba.opal.web.gwt.app.client.administration.database.edit.AbstractDatabaseModalPresenter.Usage;

/**
 *
 */
public class MongoDatabaseModalView extends ModalPopupViewWithUiHandlers<DatabaseUiHandlers>
    implements MongoDatabaseModalPresenter.Display {

  interface Binder extends UiBinder<Widget, MongoDatabaseModalView> {}

  @UiField
  Modal modal;

  @UiField
  ControlGroup nameGroup;

  @UiField
  ControlGroup urlGroup;

  @UiField
  ControlGroup usageGroup;

  @UiField
  TextBox name;

  @UiField
  TextBox url;

  @UiField
  ListBox usage;

  @UiField
  TextBox username;

  @UiField
  PasswordTextBox password;

  @UiField
  CollapsiblePanel advancedOptions;

  @UiField
  TextArea properties;

  @UiField
  TextBox batchSize;

  @UiField
  Panel defaultStorageGroup;

  @UiField
  CheckBox defaultStorage;

  private final Translations translations;

  private boolean isIdentifiers;

  @Inject
  public MongoDatabaseModalView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    init();
  }

  private void init() {
    modal.hide();
    properties.getElement().setAttribute("placeholder", translations.keyValueLabel());

    // used to support ConstraintViolation exceptions
    ConstrainedModal constrainedModal = new ConstrainedModal(modal);
    constrainedModal.registerWidget("name", translations.nameLabel(), nameGroup);
    constrainedModal.registerWidget("url", translations.urlLabel(), urlGroup);
    constrainedModal.registerWidget("usage", translations.usageLabel(), usageGroup);
  }

  @Override
  public void setIsIdentifiers(boolean isIdentifiers) {
    this.isIdentifiers = isIdentifiers;
  }

  @Override
  public void initUrl() {
    url.setText(isIdentifiers ? "mongodb://localhost:27017/opal_ids" : "mongodb://localhost:27017/opal_data");
  }

  @Override
  public void onShow() {
    name.setFocus(true);
  }

  @Override
  public void hideDialog() {
    modal.hide();
  }

  @Override
  public void setDialogMode(AbstractDatabaseModalPresenter.Mode dialogMode) {
    name.setEnabled(dialogMode == CREATE);
    modal.setTitle(dialogMode == CREATE ? translations.addMongoDatabase() : translations.editMongoDatabase());
  }

  @Override
  public void clearErrors() {
    modal.closeAlerts();
  }

  @Override
  public void showError(String message) {
    showError(null, message);
  }

  @Override
  public void showError(@Nullable FormField formField, String message) {
    ControlGroup group = null;
    if(formField != null) {
      switch(formField) {
        case NAME:
          group = nameGroup;
          break;
        case URL:
          group = urlGroup;
          break;
        case USAGE:
          group = usageGroup;
          break;
      }
    }
    if(group == null) {
      modal.addAlert(message, AlertType.ERROR);
    } else {
      modal.addAlert(message, AlertType.ERROR, group);
    }
  }

  @UiHandler("saveButton")
  public void onSave(ClickEvent event) {
    getUiHandlers().save();
  }

  @UiHandler("cancelButton")
  public void onCancel(ClickEvent event) {
    getUiHandlers().cancel();
  }

  @Override
  public HasText getName() {
    return name;
  }

  @Override
  public HasEnabled getNameEnabled() {
    return name;
  }

  @Override
  public HasVisibility getNameGroupVisibility() {
    return nameGroup;
  }

  @Override
  public void setAvailableUsages(Collection<Usage> usages) {
    for(Usage usageType : usages) {
      usage.addItem(usageType.getLabel(), usageType.name());
    }
    getUsage().setValue(Usage.STORAGE);
    usage.setEnabled(usages.size() > 1);
  }

  @Override
  public TakesValue<Usage> getUsage() {
    return new TakesValue<Usage>() {
      @Override
      public Usage getValue() {
        int selectedIndex = usage.getSelectedIndex();
        return selectedIndex < 0 ? null : Usage.valueOf(usage.getValue(selectedIndex));
      }

      @Override
      public void setValue(Usage selectedUsage) {
        if(selectedUsage == null) return;
        int count = usage.getItemCount();
        for(int i = 0; i < count; i++) {
          if(usage.getValue(i).equals(selectedUsage.name())) {
            usage.setSelectedIndex(i);
            toggleDefaultStorage(selectedUsage == Usage.STORAGE);
            break;
          }
        }
      }
    };
  }

  @Override
  public HasEnabled getUsageEnabled() {
    return usage;
  }

  @Override
  public HasChangeHandlers getUsageChangeHandlers() {
    return usage;
  }

  @Override
  public HasVisibility getUsageGroupVisibility() {
    return usageGroup;
  }

  @Override
  public HasText getUrl() {
    return url;
  }

  @Override
  public HasText getUsername() {
    return username;
  }

  public HasText getBatchSize() {
    return batchSize;
  }

  @Override
  public HasText getPassword() {
    return password;
  }

   @Override
  public HasText getProperties() {
    return new HasText() {
      @Override
      public String getText() {
        return properties.getText();
      }

      @Override
      public void setText(String text) {
        properties.setText(text);
        if(!Strings.isNullOrEmpty(text)) {
          advancedOptions.setOpen(true);
        }
      }
    };
  }

  @Override
  public HasEnabled getPropertiesEnabled() {
    return properties;
  }

  @Override
  public HasValue<Boolean> getDefaultStorage() {
    return defaultStorage;
  }

  @Override
  public HasVisibility getDefaultStorageGroupVisibility() {
    return defaultStorage;
  }

  @Override
  public void toggleDefaultStorage(boolean enabled) {
    if(!enabled) defaultStorage.setValue(false);
    defaultStorageGroup.setVisible(enabled);
  }

}
