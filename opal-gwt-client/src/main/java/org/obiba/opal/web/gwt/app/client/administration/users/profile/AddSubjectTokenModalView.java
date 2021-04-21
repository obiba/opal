/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.users.profile;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.opal.ProjectDto;
import org.obiba.opal.web.model.client.opal.SubjectTokenDto;

import java.util.List;

public class AddSubjectTokenModalView extends ModalPopupViewWithUiHandlers<AddSubjectTokenModalUiHandlers> implements AddSubjectTokenModalPresenter.Display {

  interface Binder extends UiBinder<Widget, AddSubjectTokenModalView> {
  }

  @UiField
  Modal dialog;

  @UiField
  ControlGroup nameGroup;

  @UiField
  TextBox nameText;

  @UiField
  TextBox tokenText;

  @UiField
  Chooser tokenProjects;

  @UiField
  ControlGroup dataGroup;
  @UiField
  Chooser tokenProjectData;

  @UiField
  ControlGroup tasksGroup;
  @UiField
  CheckBox importCheck;
  @UiField
  CheckBox exportCheck;
  @UiField
  CheckBox copyCheck;
  @UiField
  CheckBox analyseCheck;
  @UiField
  CheckBox reportCheck;
  @UiField
  CheckBox importVCFCheck;
  @UiField
  CheckBox exportVCFCheck;
  @UiField
  CheckBox backupCheck;
  @UiField
  CheckBox restoreCheck;

  @UiField
  ControlGroup adminGroup;
  @UiField
  CheckBox createProjectCheck;
  @UiField
  CheckBox updateProjectCheck;
  @UiField
  CheckBox deleteProjectCheck;

  @UiField
  ControlGroup servicesGroup;
  @UiField
  CheckBox rCheck;
  @UiField
  CheckBox datashieldCheck;
  @UiField
  CheckBox sqlCheck;
  @UiField
  CheckBox sysAdminCheck;

  private final Translations translations;

  private String tokenValue;

  @Inject
  public AddSubjectTokenModalView(EventBus eventBus, Binder binder, Translations translations) {
    super(eventBus);
    initWidget(binder.createAndBindUi(this));
    this.translations = translations;
    dialog.setTitle(translations.addTokenModalTitle());
    tokenText.setStyleName("password-vertical-align");
    tokenText.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        tokenText.setText(tokenValue);
      }
    });
    tokenProjects.setPlaceholderTextMultiple(translations.selectSomeProjects());
    tokenProjects.addChosenChangeHandler(new ChosenChangeEvent.ChosenChangeHandler() {
      @Override
      public void onChange(ChosenChangeEvent chosenChangeEvent) {
        enableCheckBox(createProjectCheck, tokenProjects.getValues().length == 0);
      }
    });
    tokenProjectData.addItem("Default", "");
    tokenProjectData.addItem(translations.tokenAccessMap().get(SubjectTokenDto.AccessType.READ.getName()), SubjectTokenDto.AccessType.READ.getName());
    tokenProjectData.addItem(translations.tokenAccessMap().get(SubjectTokenDto.AccessType.READ_NO_VALUES.getName()), SubjectTokenDto.AccessType.READ_NO_VALUES.getName());
    tokenProjectData.addChosenChangeHandler(new ChosenChangeEvent.ChosenChangeHandler() {
      @Override
      public void onChange(ChosenChangeEvent chosenChangeEvent) {
        onProjectDataChanged();
      }
    });
  }

  private void onProjectDataChanged() {
    String selection = tokenProjectData.getSelectedValue();
    if (SubjectTokenDto.AccessType.READ.getName().equals(selection)) {
      enableCheckBox(importCheck, false);
      enableCheckBox(exportCheck, true);
      enableCheckBox(copyCheck, false);
      enableCheckBox(importVCFCheck, false);
      enableCheckBox(exportVCFCheck, true);
      enableCheckBox(restoreCheck, false);
      enableCheckBox(backupCheck, true);
      enableCheckBox(rCheck, true);
      enableCheckBox(sqlCheck, true);
      enableCheckBox(sysAdminCheck, false);
    } else if (SubjectTokenDto.AccessType.READ_NO_VALUES.getName().equals(selection)) {
      enableCheckBox(importCheck, false);
      enableCheckBox(exportCheck, false);
      enableCheckBox(copyCheck, false);
      enableCheckBox(importVCFCheck, false);
      enableCheckBox(exportVCFCheck, false);
      enableCheckBox(restoreCheck, false);
      enableCheckBox(backupCheck, false);
      enableCheckBox(rCheck, false);
      enableCheckBox(sqlCheck, false);
      enableCheckBox(sysAdminCheck, false);
    } else {
      enableCheckBox(importCheck, true);
      enableCheckBox(exportCheck, true);
      enableCheckBox(copyCheck, true);
      enableCheckBox(importVCFCheck, true);
      enableCheckBox(exportVCFCheck, true);
      enableCheckBox(restoreCheck, true);
      enableCheckBox(backupCheck, true);
      enableCheckBox(rCheck, true);
      enableCheckBox(sqlCheck, true);
      enableCheckBox(sysAdminCheck, true);
    }
  }

  private void enableCheckBox(CheckBox check, boolean enable) {
    check.setEnabled(enable);
    if (!enable) check.setValue(false);
  }
  @UiHandler("cancelButton")
  public void onCancelButton(ClickEvent event) {
    dialog.hide();
  }

  @UiHandler("saveButton")
  public void onSave(ClickEvent event) {
    SubjectTokenDto token = SubjectTokenDto.create();
    token.setName(nameText.getText());
    token.setToken(tokenText.getText());
    for (String p : tokenProjects.getValues()) {
      token.addProjects(p);
    }

    String selection = tokenProjectData.getSelectedValue();
    if (SubjectTokenDto.AccessType.READ.getName().equals(selection))
      token.setAccess(SubjectTokenDto.AccessType.READ);
    else if (SubjectTokenDto.AccessType.READ_NO_VALUES.getName().equals(selection))
      token.setAccess(SubjectTokenDto.AccessType.READ_NO_VALUES);

    if (importCheck.getValue()) token.addCommands("import");
    if (exportCheck.getValue()) token.addCommands("export");
    if (copyCheck.getValue()) token.addCommands("copy");
    if (analyseCheck.getValue()) token.addCommands("analyse");
    if (reportCheck.getValue()) token.addCommands("report");
    if (importVCFCheck.getValue()) token.addCommands("import_vcf");
    if (exportVCFCheck.getValue()) token.addCommands("export_vcf");
    if (backupCheck.getValue()) token.addCommands("backup");
    if (restoreCheck.getValue()) token.addCommands("restore");

    token.setCreateProject(createProjectCheck.getValue() && tokenProjects.getValues().length == 0);
    token.setUpdateProject(updateProjectCheck.getValue());
    token.setDeleteProject(deleteProjectCheck.getValue());

    token.setUseR(rCheck.getValue());
    token.setUseDatashield(datashieldCheck.getValue());
    token.setUseSQL(sqlCheck.getValue());
    token.setSysAdmin(sysAdminCheck.getValue());
    getUiHandlers().onCreateToken(token);
  }

  @UiHandler("copyTokenButton")
  public void onGenerateToken(ClickEvent event) {
    tokenText.setFocus(true);
    tokenText.selectAll();
    copyToClipboard();
  }

  @Override
  public void onShow() {
    tokenValue = getUiHandlers().onGenerateToken();
    tokenText.setText(tokenValue);
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public void showError(String message) {
    dialog.closeAlerts();
    dialog.addAlert(message, AlertType.ERROR, nameGroup);
  }

  @Override
  public void setProjects(List<ProjectDto> projects) {
    tokenProjects.clear();
    for(ProjectDto project : projects) {
      this.tokenProjects.addItem(project.getName());
    }
  }

  @Override
  public void setPurpose(Purpose purpose) {
    hideAdditionalGroups();
    switch (purpose) {
      case DATASHIELD:
        dialog.setTitle(translations.addDataSHIELDTokenModalTitle());
        tokenProjectData.setSelectedValue(SubjectTokenDto.AccessType.READ_NO_VALUES.getName());
        datashieldCheck.setValue(true);
        break;
      case R:
        dialog.setTitle(translations.addRTokenModalTitle());
        dataGroup.setVisible(true);
        rCheck.setValue(true);
        copyCheck.setVisible(false);
        backupCheck.setVisible(false);
        restoreCheck.setVisible(false);
        reportCheck.setVisible(false);
        analyseCheck.setVisible(false);
        importVCFCheck.setVisible(false);
        exportVCFCheck.setVisible(false);
        tasksGroup.setVisible(true);
        break;
      case SQL:
        dialog.setTitle(translations.addSQLTokenModalTitle());
        tokenProjectData.setSelectedValue(SubjectTokenDto.AccessType.READ.getName());
        sqlCheck.setValue(true);
        break;
    }
    onProjectDataChanged();
  }

  private void hideAdditionalGroups() {
    dataGroup.setVisible(false);
    tasksGroup.setVisible(false);
    adminGroup.setVisible(false);
    servicesGroup.setVisible(false);
  }

  private static native boolean copyToClipboard() /*-{
      return $doc.execCommand('copy');
  }-*/;
}
