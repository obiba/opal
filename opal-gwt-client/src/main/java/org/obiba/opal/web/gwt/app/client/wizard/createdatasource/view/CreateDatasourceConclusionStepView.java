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
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ResourceRequestPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.CreateDatasourceConclusionStepPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.DatasourceParsingErrorTable;
import org.obiba.opal.web.model.client.magma.DatasourceParsingErrorDto.ClientErrorDtoExtensions;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class CreateDatasourceConclusionStepView extends Composite implements CreateDatasourceConclusionStepPresenter.Display {

  @UiTemplate("CreateDatasourceConclusionStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, CreateDatasourceConclusionStepView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  @UiField
  Label failed;

  @UiField
  SimplePanel datasourcePanel;

  @UiField
  DatasourceParsingErrorTable datasourceParsingErrorTable;

  private Translations translations = GWT.create(Translations.class);

  public CreateDatasourceConclusionStepView() {
    initWidget(uiBinder.createAndBindUi(this));
    failed.setVisible(false);
    datasourceParsingErrorTable.setVisible(false);
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

  @Override
  public void setDatasourceRequestDisplay(ResourceRequestPresenter.Display resourceRequestDisplay) {
    datasourcePanel.setWidget(resourceRequestDisplay.asWidget());
  }

  @Override
  public void setCompleted() {
  }

  @Override
  public void setFailed(ClientErrorDto errorDto) {
    failed.setVisible(true);

    if(errorDto != null && errorDto.getExtension(ClientErrorDtoExtensions.errors) != null) {
      datasourceParsingErrorTable.setErrors(errorDto);
      datasourceParsingErrorTable.setVisible(true);
    }
  }

}
