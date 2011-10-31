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

import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

public abstract class DerivationPresenter<D extends WidgetDisplay> extends WidgetPresenter<D> {

  protected VariableDto originalVariable;

  public DerivationPresenter(D display, EventBus eventBus) {
    super(display, eventBus);
  }

  void setOriginalVariable(VariableDto variable) {
    this.originalVariable = variable;
  }

  abstract VariableDto getDerivedVariable();

  abstract List<DefaultWizardStepController> getWizardSteps();

  protected VariableDto copyOriginalVariable() {
    VariableDto derived = VariableDto.create();
    derived.setName(originalVariable.getName());
    derived.setValueType(originalVariable.getValueType());

    // set attributes
    derived.setAttributesArray(originalVariable.getAttributesArray());

    return derived;
  }

  protected void setScript(VariableDto derived, String script) {
    AttributeDto scriptAttr = getScriptAttribute(derived);
    scriptAttr.setValue(script);
  }

  public String getScript(VariableDto derived) {
    return getScriptAttribute(derived).getValue();
  }

  private AttributeDto getScriptAttribute(VariableDto derived) {
    AttributeDto scriptAttr = null;
    for(AttributeDto attr : JsArrays.toIterable(derived.getAttributesArray())) {
      if(attr.getName().equals("script")) {
        scriptAttr = attr;
        break;
      }
    }
    if(scriptAttr == null) {
      scriptAttr = AttributeDto.create();
      scriptAttr.setName("script");
      scriptAttr.setValue("null");
      derived.getAttributesArray().push(scriptAttr);
    }
    return scriptAttr;
  }

}