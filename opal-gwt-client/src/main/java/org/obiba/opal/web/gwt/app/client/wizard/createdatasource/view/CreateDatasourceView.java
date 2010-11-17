/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.createdatasource.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.CreateDatasourceConclusionStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.CreateDatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.DatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.CreateDatasourceConclusionStepPresenter.DatasourceCreatedCallback;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardDialogBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class CreateDatasourceView extends Composite implements CreateDatasourcePresenter.Display {
  //
  // Static Variables
  //

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  //
  // Instance Variables
  //

  @UiField
  WizardDialogBox dialog;

  @UiField
  WizardStep selectTypeStep;

  @UiField
  WizardStep datasourceFormStep;

  @UiField
  WizardStep conclusionStep;

  @UiField
  TextBox datasourceName;

  @UiField
  ListBox datasourceType;

  private DatasourceFormPresenter datasourceFormPresenter;

  private ValidationHandler selectTypeValidator;

  //
  // Constructors
  //

  public CreateDatasourceView() {
    initWidget(uiBinder.createAndBindUi(this));
    uiBinder.createAndBindUi(this);
    initWizardDialog();
    initSelectTypeStep();
    initDatasourceFormStep();
    initConclusionStep();
  }

  private void initWizardDialog() {
    dialog.setGlassEnabled(false);
    dialog.hide();
  }

  private void initSelectTypeStep() {
    selectTypeStep.setStepTitle(translations.createDatasourceStepSummary());
    dialog.setNextEnabled(true);
  }

  private void initDatasourceFormStep() {
    datasourceFormStep.setStepTitle(translations.datasourceOptionsLabel());
    dialog.setNextEnabled(false);
    dialog.addPreviousClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        if(datasourceFormStep.isVisible()) {
          datasourceFormStep.setVisible(false);
          selectTypeStep.setVisible(true);
          dialog.setPreviousEnabled(false);
        }
      }
    });
  }

  private void initConclusionStep() {
    conclusionStep.setStepTitle(translations.createDatasourceProcessSummary());
    conclusionStep.setVisible(false);
  }

  private void clear() {
    datasourceName.setText("");
    selectTypeStep.setVisible(true);

    datasourceFormStep.setVisible(false);

    conclusionStep.setStepTitle(translations.createDatasourceProcessSummary());
    conclusionStep.setVisible(false);
    conclusionStep.removeStepContent();

    dialog.setNextEnabled(true);
    dialog.setPreviousEnabled(false);
    dialog.setFinishEnabled(false);
    dialog.setCancelEnabled(true);

  }

  private boolean hasDatasourceForm() {
    return datasourceFormPresenter != null;
  }

  //
  // CreateViewStepPresenter.Display Methods
  //

  @Override
  public HandlerRegistration addCancelClickHandler(ClickHandler handler) {
    return dialog.addCancelClickHandler(handler);
  }

  @Override
  public HandlerRegistration addFinishClickHandler(ClickHandler handler) {
    return dialog.addFinishClickHandler(handler);
  }

  @Override
  public HandlerRegistration addCreateClickHandler(final ClickHandler handler) {
    return dialog.addNextClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent evt) {
        if(selectTypeStep.isVisible()) {
          if(selectTypeValidator.validate()) {
            selectTypeStep.setVisible(false);
            if(hasDatasourceForm()) {
              datasourceFormStep.setVisible(true);
              dialog.setPreviousEnabled(true);
            }
          }
        } else if(datasourceFormStep.isVisible()) {
          handler.onClick(evt);
        }
      }
    });
  }

  @Override
  public HandlerRegistration addDatasourceTypeChangeHandler(ChangeHandler handler) {
    return datasourceType.addChangeHandler(handler);
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  //
  // Inner Classes / Interfaces
  //

  @UiTemplate("CreateDatasourceView.ui.xml")
  interface ViewUiBinder extends UiBinder<DialogBox, CreateDatasourceView> {
  }

  @Override
  public void showDialog() {
    clear();
    dialog.center();
    dialog.show();
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public HasText getDatasourceName() {
    return datasourceName;
  }

  @Override
  public String getDatasourceType() {
    return datasourceType.getItemText(datasourceType.getSelectedIndex());
  }

  @Override
  public void setDatasourceForm(DatasourceFormPresenter formPresenter) {
    if(datasourceFormPresenter != null) {
      datasourceFormPresenter.unbind();
      datasourceFormStep.removeStepContent();
    }
    datasourceFormPresenter = formPresenter;
    if(datasourceFormPresenter != null) {
      datasourceFormPresenter.bind();
      datasourceFormStep.add(formPresenter.getDisplay().asWidget());
    }
  }

  @Override
  public DatasourceFormPresenter getDatasourceForm() {
    return datasourceFormPresenter;
  }

  @Override
  public void setConclusion(CreateDatasourceConclusionStepPresenter presenter) {
    datasourceFormStep.setVisible(false);
    presenter.bind();
    conclusionStep.add(presenter.getDisplay().asWidget());
    conclusionStep.setVisible(true);
    dialog.setPreviousEnabled(false);
    dialog.setNextEnabled(false);
    dialog.setCancelEnabled(false);
    dialog.setFinishEnabled(false);
    presenter.setDatasourceCreatedCallback(new DatasourceCreatedCallback() {

      @Override
      public void onSuccess(DatasourceFactoryDto factory) {
        conclusionStep.setStepTitle(translations.datasourceCreationCompleted());
        dialog.setFinishEnabled(true);
      }

      @Override
      public void onFailure(DatasourceFactoryDto factory, ClientErrorDto error) {
        conclusionStep.setStepTitle(translations.datasourceCreationFailed());
        dialog.setCancelEnabled(true);
      }
    });

  }

  @Override
  public void setDatasourceSelectionTypeValidationHandler(ValidationHandler handler) {
    this.selectTypeValidator = handler;
  }
}
