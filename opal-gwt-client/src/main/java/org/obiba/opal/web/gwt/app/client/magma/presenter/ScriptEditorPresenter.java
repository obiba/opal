/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.presenter;

import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.support.VariableDtos;
import org.obiba.opal.web.gwt.app.client.support.VariableDtos.ValueType;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class ScriptEditorPresenter extends PresenterWidget<ScriptEditorPresenter.Display> {

  private TableDto table;

  private String variableName;

  private final ModalProvider<ScriptEvaluationPopupPresenter> scriptEvaluationPopupProvider;

  private VariableDtoFactory variableDtoFactory = new DefaultVariableDtoFactory();

  @Inject
  public ScriptEditorPresenter(EventBus eventBus, Display view,
      ModalProvider<ScriptEvaluationPopupPresenter> scriptEvaluationPopupProvider) {
    super(eventBus, view);
    this.scriptEvaluationPopupProvider = scriptEvaluationPopupProvider.setContainer(this);
  }

  @Override
  protected void onBind() {
    addEventHandlers();
  }

  private void addEventHandlers() {
    registerHandler(getView().addTestScriptClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        scriptEvaluationPopupProvider.get().initialize(table, getVariableDtoFactory().create());
      }
    }));
  }

  public void setView(ViewDto viewDto) {
    UriBuilder ub = UriBuilder.create().segment("datasource", viewDto.getDatasourceName(), "table", viewDto.getName());
    ResourceRequestBuilderFactory.<TableDto>newBuilder().forResource(ub.build()).get()
        .withCallback(new ResourceCallback<TableDto>() {
          @Override
          public void onResource(Response response, TableDto resource) {
            setTable(resource);
            setVariableName(variableName);
          }

        }).send();
  }

  public void setTable(TableDto table) {
    this.table = table;
  }

  public void setVariableName(String variableName) {
    this.variableName = variableName;
  }

  public void setRepeatable(boolean repeatable) {
    getView().setIsRepeatable(repeatable);
  }

  public void setScript(String script) {
    getView().setScript(script);
  }

  public String getSelectedScript() {
    return getView().getSelectedScript();
  }

  public void setValueEntityType(String value) {
    getView().setValueType(value);
  }

  public ValueType getValueEntityType() {
    return getView().getValueType();
  }

  public boolean isRepeatable() {
    return getView().isRepeatable();
  }

  public String getScript() {
    String script = getView().getScript();
    return "".equals(script.trim()) ? "null" : script;
  }

  public String getBeautifiedScript() {
    String script = getView().getBeautifiedScript();
    return "".equals(script.trim()) ? "null" : script;
  }

  public void showTest(boolean b) {
    getView().showTest(b);
  }

  public void setVariableDtoFactory(VariableDtoFactory variableDtoFactory) {
    this.variableDtoFactory = variableDtoFactory;
  }

  public VariableDtoFactory getVariableDtoFactory() {
    return variableDtoFactory;
  }

  public interface VariableDtoFactory {
    VariableDto create();
  }

  public class DefaultVariableDtoFactory implements VariableDtoFactory {

    @Override
    public VariableDto create() {
      String selectedScript = getView().getSelectedScript();
      VariableDto derived = VariableDto.create();
      derived.setName(variableName);
      String script = null;

      if(Strings.isNullOrEmpty(selectedScript)) {
        derived.setValueType(getValueEntityType().getLabel());
        derived.setIsRepeatable(isRepeatable());
        script = getScript();
      } else {
        derived.setValueType(ValueType.TEXT.getLabel());
        derived.setIsRepeatable(false);
        script = selectedScript;
      }

      VariableDtos.setScript(derived, script);
      return derived;
    }
  }

  public interface Display extends View {

    String getScript();

    void showTest(boolean b);

    void setScript(String script);

    String getSelectedScript();

    ValueType getValueType();

    boolean isRepeatable();

    HandlerRegistration addTestScriptClickHandler(ClickHandler handler);

    void formEnable(boolean enabled);

    void formClear();

    HandlerRegistration addScriptChangeHandler(ChangeHandler handler);

    String getBeautifiedScript();

    void setIsRepeatable(boolean repeatable);

    void setValueType(String value);
  }

}
