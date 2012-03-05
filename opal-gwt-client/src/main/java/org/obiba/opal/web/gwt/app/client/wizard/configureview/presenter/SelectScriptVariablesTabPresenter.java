/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSavePendingEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSaveRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSavedEvent;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.EvaluateScriptPresenter;
import org.obiba.opal.web.model.client.magma.JavaScriptViewDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 * Variables tab used to specify a view's variables by means of a JavaScript "select" script.
 * 
 * The "select" script tests each variable in the view's underlying tables and returns <code>true</code> if the variable
 * is to be included in the view.
 */
public class SelectScriptVariablesTabPresenter extends PresenterWidget<SelectScriptVariablesTabPresenter.Display> {
  //
  // Instance Variables
  //

  /**
   * The {@link ViewDto} of the view being configured.
   * 
   * When the tab's save button is pressed, changes are applied to this ViewDto (i.e., to its JavaScriptViewDto
   * extension).
   */
  private ViewDto viewDto;

  /**
   * Widget for entering, and testing, the "select" script.
   */
  private EvaluateScriptPresenter evaluateScriptPresenter;

  private Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

  @Inject
  public SelectScriptVariablesTabPresenter(final Display display, final EventBus eventBus, EvaluateScriptPresenter evaluateScriptPresenter) {
    super(eventBus, display);
    this.evaluateScriptPresenter = evaluateScriptPresenter;
  }

  @Override
  protected void onBind() {
    evaluateScriptPresenter.showTest(false);
    setInSlot(Display.Slots.Test, evaluateScriptPresenter);

    getView().saveChangesEnabled(false);

    addEventHandlers();
  }

  public void setViewDto(ViewDto viewDto) {
    this.viewDto = viewDto;

    viewDto.setFromArray(JsArrays.toSafeArray(viewDto.getFromArray()));
    evaluateScriptPresenter.setTable(viewDto);

    JavaScriptViewDto jsViewDto = (JavaScriptViewDto) viewDto.getExtension(JavaScriptViewDto.ViewDtoExtensions.view);
    if(jsViewDto != null && jsViewDto.hasSelect()) {
      getView().setVariablesToView(VariablesToView.SCRIPT);
      evaluateScriptPresenter.setScript(jsViewDto.getSelect());
    } else {
      getView().setVariablesToView(VariablesToView.ALL);
      evaluateScriptPresenter.setScript("");
    }
  }

  private void addEventHandlers() {
    super.registerHandler(getEventBus().addHandler(ViewConfigurationRequiredEvent.getType(), new ViewConfigurationRequiredEventHandler()));
    super.registerHandler(getView().addSaveChangesClickHandler(new SaveChangesClickHandler()));
    super.registerHandler(getEventBus().addHandler(ViewSavedEvent.getType(), new ViewSavedHandler()));
    super.registerHandler(getView().addVariablestoViewChangeHandler(new VariablesToViewChangeHandler()));
    super.registerHandler(evaluateScriptPresenter.getView().addScriptChangeHandler(new ScriptChangeHandler()));
  }

  private boolean validate() {
    List<String> messages = new ArrayList<String>();
    String message;
    for(FieldValidator validator : validators) {
      message = validator.validate();
      if(message != null) {
        messages.add(message);
      }
    }

    if(messages.size() > 0) {
      getEventBus().fireEvent(NotificationEvent.newBuilder().error(messages).build());
      return false;
    } else {
      return true;
    }
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends View {

    enum Slots {
      Test
    }

    Widget getHelpWidget();

    void saveChangesEnabled(boolean enabled);

    void setScriptWidgetVisible(boolean visible);

    void setVariablesToView(VariablesToView scriptOrAll);

    VariablesToView getVariablesToView();

    HandlerRegistration addSaveChangesClickHandler(ClickHandler clickHandler);

    HandlerRegistration addVariablestoViewChangeHandler(ChangeHandler changeHandler);

    ListBox getVariablesToViewListBox();
  }

  public enum VariablesToView {
    SCRIPT, ALL
  }

  class ViewConfigurationRequiredEventHandler implements ViewConfigurationRequiredEvent.Handler {

    @Override
    public void onViewConfigurationRequired(ViewConfigurationRequiredEvent event) {
      SelectScriptVariablesTabPresenter.this.setViewDto(event.getView());
    }
  }

  class SaveChangesClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      if(validate()) {
        updateViewDto();
      }
    }

    private ViewDto getViewDto() {
      return viewDto;
    }

    private void updateViewDto() {
      JavaScriptViewDto jsViewDto = (JavaScriptViewDto) viewDto.getExtension(JavaScriptViewDto.ViewDtoExtensions.view);

      if(getView().getVariablesToView().equals(VariablesToView.SCRIPT)) {
        String script = evaluateScriptPresenter.getScript().trim();
        if(script.length() != 0) {
          jsViewDto.setSelect(script);
        } else {
          jsViewDto.clearSelect();
        }
      } else {
        jsViewDto.clearSelect();
      }

      getEventBus().fireEvent(new ViewSaveRequiredEvent(getViewDto()));
    }
  }

  class ViewSavedHandler implements ViewSavedEvent.Handler {

    @Override
    public void onViewSaved(ViewSavedEvent event) {
      getView().saveChangesEnabled(false);
    }
  }

  class VariablesToViewChangeHandler implements ChangeHandler {

    @Override
    public void onChange(ChangeEvent event) {
      if(getView().getVariablesToView() == VariablesToView.ALL) {
        evaluateScriptPresenter.setScript("");
      }
      getView().setScriptWidgetVisible(getView().getVariablesToView().equals(VariablesToView.SCRIPT));
      getView().saveChangesEnabled(true);
      getEventBus().fireEvent(new ViewSavePendingEvent());
    }
  }

  class ScriptChangeHandler implements ChangeHandler {

    @Override
    public void onChange(ChangeEvent event) {
      getView().saveChangesEnabled(true);
      getEventBus().fireEvent(new ViewSavePendingEvent());
    }
  }
}