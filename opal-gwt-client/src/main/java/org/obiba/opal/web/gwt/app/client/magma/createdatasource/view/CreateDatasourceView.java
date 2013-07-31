/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.createdatasource.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.WizardModalBox;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepChain;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepController.ResetHandler;
import org.obiba.opal.web.gwt.app.client.magma.createdatasource.presenter.CreateDatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.ui.ModalViewImpl;
import org.obiba.opal.web.gwt.app.client.ui.WizardStep;

import com.github.gwtbootstrap.client.ui.Modal;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CreateDatasourceView extends ModalViewImpl implements CreateDatasourcePresenter.Display {

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  WizardModalBox dialog;

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

  @UiField
  HTMLPanel helpPanel;

  private ValidationHandler selectTypeValidator;

  private WizardStepChain stepChain;

  @Inject
  public CreateDatasourceView(EventBus eventBus) {
    super(eventBus);
    widget = uiBinder.createAndBindUi(this);
    initWizardDialog();
    for(int i = 0; i < datasourceType.getItemCount(); i++) {
      datasourceType.setItemText(i, translations.datasourceTypeMap().get(datasourceType.getValue(i)));
    }
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    datasourceFormStep.removeStepContent();
    if(content != null) {
      datasourceFormStep.add(content.asWidget());
    }
  }

  private void initWizardDialog() {
    stepChain = WizardStepChain.Builder.create(dialog)//

        .append(selectTypeStep, helpPanel)//
        .title(translations.createDatasourceStepSummary())//
        .onValidate(new ValidationHandler() {

          @Override
          public boolean validate() {
            return selectTypeValidator.validate();
          }
        })//
        .onReset(new ResetHandler() {

          @Override
          public void onReset() {
            datasourceName.setText("");
            datasourceType.setSelectedIndex(0);
          }
        })//

        .append(datasourceFormStep)//
        .title(translations.datasourceOptionsLabel())//

        .append(conclusionStep)//
        .title(translations.createDatasourceProcessSummary())//
        .conclusion()//

        .onNext().onPrevious().build();
  }

  //
  // CreateViewStepPresenter.Display Methods
  //

  @Override
  public HandlerRegistration addCancelClickHandler(ClickHandler handler) {
    return dialog.addCancelClickHandler(handler);
  }

  @Override
  public HandlerRegistration addCloseClickHandler(ClickHandler handler) {
    return dialog.addCloseClickHandler(handler);
  }

  @Override
  public HandlerRegistration addFinishClickHandler(final ClickHandler handler) {
    return dialog.addFinishClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent evt) {
        // asynchronous next, see setConclusion()
        handler.onClick(evt);
      }
    });
  }

  @Override
  public HandlerRegistration addDatasourceTypeChangeHandler(ChangeHandler handler) {
    return datasourceType.addChangeHandler(handler);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  protected Modal asModal() {
    return dialog;
  }

  //
  // Inner Classes / Interfaces
  //

  interface ViewUiBinder extends UiBinder<Widget, CreateDatasourceView> {}

  @Override
  public void show() {
    stepChain.reset();
    super.show();
  }

  @Override
  public HasText getDatasourceName() {
    return datasourceName;
  }

  @Override
  public String getDatasourceType() {
    return datasourceType.getValue(datasourceType.getSelectedIndex());
  }

  @Override
  public void setDatasourceSelectionTypeValidationHandler(ValidationHandler handler) {
    selectTypeValidator = handler;
  }
}
