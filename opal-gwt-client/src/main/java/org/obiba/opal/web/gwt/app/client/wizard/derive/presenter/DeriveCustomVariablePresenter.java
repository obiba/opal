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

import org.obiba.opal.web.gwt.app.client.widgets.event.ScriptEvaluationPopupEvent;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController.Builder;
import org.obiba.opal.web.gwt.app.client.wizard.derive.helper.DerivedVariableGenerator;
import org.obiba.opal.web.gwt.app.client.wizard.derive.util.Variables;
import org.obiba.opal.web.gwt.app.client.wizard.derive.util.Variables.ValueType;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.widget.ScriptSuggestBox;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.LinkDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DeriveCustomVariablePresenter extends DerivationPresenter<DeriveCustomVariablePresenter.Display> {

  @Inject
  public DeriveCustomVariablePresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  void initialize(VariableDto variable) {
    super.initialize(variable);
    display.getRepeatable().setValue(variable.getIsRepeatable());
    display.getTestButton().addClickHandler(new TestButtonClickHandler());
    display.getValueType().setValue(variable.getValueType());
    display.getScriptBox().setValue("$('" + originalVariable.getName() + "')");
    display.addSuggestions(variable.getParentLink());
  }

  class TestButtonClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      ResourceRequestBuilderFactory.<TableDto> newBuilder().forResource(originalVariable.getParentLink().getLink()).get().withCallback(new ResourceCallback<TableDto>() {
        @Override
        public void onResource(Response response, TableDto table) {
          String selectedScript = display.getScriptBox().getSelectedScript();
          VariableDto variable = getDerivedVariable();
          if(!Strings.isNullOrEmpty(selectedScript)) {
            variable.setValueType(ValueType.TEXT.getLabel());
            variable.setIsRepeatable(false);
            Variables.setScript(variable, selectedScript);
          }
          eventBus.fireEvent(new ScriptEvaluationPopupEvent(variable, table));
        }
      }).send();
    }
  }

  @Override
  public void refreshDisplay() {

  }

  @Override
  public void revealDisplay() {

  }

  @Override
  public VariableDto getDerivedVariable() {
    VariableDto derived = DerivedVariableGenerator.copyVariable(originalVariable, false);
    derived.setIsRepeatable(display.getRepeatable().getValue());
    DerivedVariableGenerator.setScript(derived, display.getScriptBox().getValue());
    derived.setValueType(display.getValueType().getValue());
    return derived;
  }

  @Override
  List<DefaultWizardStepController> getWizardSteps() {
    List<DefaultWizardStepController> stepCtrls = new ArrayList<DefaultWizardStepController>();
    stepCtrls.add(getDisplay().getDeriveStepController().build());
    return stepCtrls;
  }

  @Override
  protected void onBind() {
  }

  @Override
  protected void onUnbind() {
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

    HasClickHandlers getTestButton();

    void addSuggestions(LinkDto parentLink);

    void add(Widget widget);

    ScriptSuggestBox getScriptBox();

    HasValue<String> getValueType();

    HasValue<Boolean> getRepeatable();

  }
}
