/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.derive.presenter;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController.Builder;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DeriveCustomVariablePresenter extends DerivationPresenter<DeriveCustomVariablePresenter.Display> {

  @Inject
  ScriptDesignerPresenter scriptDesignerPresenter;

  @Inject
  public DeriveCustomVariablePresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public void refreshDisplay() {

  }

  @Override
  public void revealDisplay() {

  }

  @Override
  public VariableDto getDerivedVariable() {
    return null;
  }

  @Override
  List<DefaultWizardStepController> getWizardSteps() {
    List<DefaultWizardStepController> stepCtrls = new ArrayList<DefaultWizardStepController>();
    stepCtrls.add(getDisplay().getDeriveStepController().build());
    return stepCtrls;
  }

  @Override
  protected void onBind() {
    scriptDesignerPresenter.bind();
    getDisplay().add(scriptDesignerPresenter.getDisplay().asWidget());
  }

  @Override
  protected void onUnbind() {
    scriptDesignerPresenter.unbind();
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  public interface Display extends WidgetDisplay {

    Builder getDeriveStepController();

    void add(Widget widget);

  }
}
