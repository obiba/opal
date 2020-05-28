/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.edit;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.support.DatasourceDtos;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.validator.ConstrainedModal;
import org.obiba.opal.web.model.client.database.DatabaseDto;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;
import org.obiba.opal.web.model.client.opal.ProjectDto;

import javax.annotation.Nullable;
import java.util.List;

public class EditProjectModalView extends ModalPopupViewWithUiHandlers<EditProjectUiHandlers>
    implements EditProjectModalPresenter.Display {

  private static final String DATABASE_NONE = "_none";
  private static final String VCF_STORE_SERVICE_NONE = "_none";

  interface Binder extends UiBinder<Widget, EditProjectModalView> {
  }

  @UiField
  Modal modal;

  @UiField
  ControlGroup nameGroup;

  @UiField
  TextBox name;

  @UiField
  HasText title;

  @UiField
  ControlGroup databaseGroup;

  @UiField
  Chooser database;

  @UiField
  HasText description;

  @UiField
  HasText tags;

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  @UiField
  Chooser vcfStoreService;

  @UiField
  ControlGroup vcfStoreServiceGroup;

  @UiField
  ControlGroup exportFolderGroup;

  @UiField
  SimplePanel exportFolderPanel;

  private FileSelectionPresenter.Display folderSelection;

  private final Translations translations;

  @Inject
  public EditProjectModalView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    initWidget(uiBinder.createAndBindUi(this));

    this.translations = translations;
    modal.setTitle(translations.addProject());
    database.setPlaceholderText(translations.selectDatabase());

    ConstrainedModal constrainedModal = new ConstrainedModal(modal);
    constrainedModal.registerWidget("name", translations.nameLabel(), nameGroup);
  }

  @Override
  public void setFileWidgetDisplay(FileSelectionPresenter.Display display) {
    exportFolderPanel.setWidget(display.asWidget());
    folderSelection = display;
    folderSelection.setEnabled(true);
    folderSelection.setFieldWidth("20em");
  }

  @Override
  public void setProject(ProjectDto project) {
    modal.setTitle(translations.editProperties());
    name.setText(project.getName());
    name.setEnabled(false);
    title.setText(project.getTitle());
    description.setText(project.getDescription());
    if (project.getTagsArray() != null) tags.setText(project.getTagsArray().join(", "));
    // database will be set when databases list will be available
    database.setEnabled(!DatasourceDtos.hasPersistedTables(project.getDatasource()));
  }

  @Override
  public void setTag(String tag) {
    tags.setText(tag);
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
  public HasText getTags() {
    return tags;
  }

  @Override
  public HasText getDatabase() {
    return new HasText() {
      @Override
      public String getText() {
        String selectedDatabase = database.getSelectedValue();
        return selectedDatabase == null || DATABASE_NONE.equals(selectedDatabase) ? null : selectedDatabase;
      }

      @Override
      public void setText(@Nullable String text) {
        if (Strings.isNullOrEmpty(text)) return;
        int count = database.getItemCount();
        for (int i = 0; i < count; i++) {
          if (database.getValue(i).equals(text)) {
            database.setSelectedIndex(i);
            break;
          }
        }
      }
    };
  }

  @Override
  public HasText getVcfStoreService() {
    return new HasText() {
      @Override
      public String getText() {
        String selectedVcfStoreService = vcfStoreService.getSelectedValue();
        return selectedVcfStoreService == null || VCF_STORE_SERVICE_NONE.equals(selectedVcfStoreService) ? null : selectedVcfStoreService;
      }

      @Override
      public void setText(String s) {
        if (Strings.isNullOrEmpty(s)) return;
        int count = vcfStoreService.getItemCount();
        for (int i = 0; i < count; i++) {
          if (vcfStoreService.getValue(i).equals(s)) {
            vcfStoreService.setSelectedIndex(i);
            break;
          }
        }
      }
    };
  }

  @Override
  public HasText getExportFolder() {
    return folderSelection.getFileText();
  }

  @Override
  public void setExportFolder(String exportFolder) {
    if (!Strings.isNullOrEmpty(exportFolder)) folderSelection.setFile(exportFolder);
  }

  @Override
  public void setAvailableDatabases(JsArray<DatabaseDto> availableDatabases) {
    database.clear();
    database.addItem("(" + translations.none() + ")", DATABASE_NONE);

    String defaultStorageDatabase = DATABASE_NONE;
    for (DatabaseDto databaseDto : JsArrays.toIterable(availableDatabases)) {
      StringBuilder label = new StringBuilder(databaseDto.getName());
      if (databaseDto.getDefaultStorage()) {
        defaultStorageDatabase = databaseDto.getName();
        label.append(" (").append(translations.defaultStorage().toLowerCase()).append(")");
      }
      database.addItem(label.toString(), databaseDto.getName());
    }
    getDatabase().setText(defaultStorageDatabase);
  }

  @Override
  public void setBusy(boolean busy) {
    modal.setBusy(busy);
    modal.setCloseVisible(!busy);
    saveButton.setEnabled(!busy);
    cancelButton.setEnabled(!busy);
  }

  @Override
  public void setAvailableVcfStoreServices(List<PluginPackageDto> installedVcfStoreServices) {
    vcfStoreService.clear();
    vcfStoreService.addItem("(" + translations.none() + ")", VCF_STORE_SERVICE_NONE);

    for (PluginPackageDto pluginPackageDto : installedVcfStoreServices) {
      vcfStoreService.addItem(pluginPackageDto.getName());
    }

    vcfStoreServiceGroup.setVisible(!installedVcfStoreServices.isEmpty());
  }

  @Override
  public void showError(@Nullable FormField formField, String message) {
    ControlGroup group = null;

    if (formField == FormField.NAME) {
      group = nameGroup;
    } else if (formField == FormField.DATABASE) {
      group = databaseGroup;
    }

    if (group == null) {
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
  public void hideDialog() {
    modal.hide();
  }

}
