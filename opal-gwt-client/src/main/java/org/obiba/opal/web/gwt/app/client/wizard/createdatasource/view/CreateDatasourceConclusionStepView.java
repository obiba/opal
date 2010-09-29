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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
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
  Label summary;

  @UiField
  Label completed;

  @UiField
  Label failed;

  @UiField
  SimplePanel datasourcePanel;

  @UiField
  Button returnButton;

  private Translations translations = GWT.create(Translations.class);

  public CreateDatasourceConclusionStepView() {
    initWidget(uiBinder.createAndBindUi(this));
    completed.setVisible(false);
    failed.setVisible(false);
    returnButton.setVisible(false);
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
  public HandlerRegistration addReturnClickHandler(ClickHandler handler) {
    return returnButton.addClickHandler(handler);
  }

  @Override
  public void setDatasourceRequestDisplay(ResourceRequestPresenter.Display resourceRequestDisplay) {
    datasourcePanel.setWidget(resourceRequestDisplay.asWidget());
  }

  @Override
  public void setCompleted() {
    summary.setText(translations.datasourceCreationCompleted());
    completed.setVisible(true);
    returnButton.setVisible(true);
  }

  @Override
  public void setFailed() {
    summary.setText(translations.datasourceCreationFailed());
    failed.setVisible(true);
    returnButton.setVisible(true);
  }

}
