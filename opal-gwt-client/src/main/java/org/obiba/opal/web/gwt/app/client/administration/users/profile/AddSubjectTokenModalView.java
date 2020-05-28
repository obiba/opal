/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
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
  Button cancelButton;

  @UiField
  ControlGroup nameGroup;

  @UiField
  TextBox nameText;

  @UiField
  TextBox tokenText;

  @UiField
  Chooser tokenProjects;

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
  CheckBox rCheck;
  @UiField
  CheckBox datashieldCheck;
  @UiField
  CheckBox sysAdminCheck;

  private String tokenValue;

  @Inject
  public AddSubjectTokenModalView(EventBus eventBus, Binder binder, Translations translations) {
    super(eventBus);
    initWidget(binder.createAndBindUi(this));
    dialog.setTitle(translations.addTokenModalTitle());
    tokenText.setStyleName("password-vertical-align");
    tokenText.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        tokenText.setText(tokenValue);
      }
    });
    tokenProjects.setPlaceholderTextMultiple(translations.selectSomeProjects());
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
    if (importCheck.isChecked()) token.addCommands("import");
    if (exportCheck.isChecked()) token.addCommands("export");
    if (copyCheck.isChecked()) token.addCommands("copy");
    if (analyseCheck.isChecked()) token.addCommands("analyse");
    if (reportCheck.isChecked()) token.addCommands("report");
    if (importVCFCheck.isChecked()) token.addCommands("import_vcf");
    if (exportVCFCheck.isChecked()) token.addCommands("export_vcf");
    token.setUseR(rCheck.isChecked());
    token.setUseDatashield(datashieldCheck.isChecked());
    token.setSysAdmin(sysAdminCheck.isChecked());
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

  private static native boolean copyToClipboard() /*-{
      return $doc.execCommand('copy');
  }-*/;
}
