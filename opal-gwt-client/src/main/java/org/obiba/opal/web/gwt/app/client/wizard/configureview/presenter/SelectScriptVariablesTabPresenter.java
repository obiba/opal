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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSavePendingEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSaveRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSavedEvent;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.EvaluateScriptPresenter;
import org.obiba.opal.web.model.client.magma.JavaScriptViewDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Variables tab used to specify a view's variables by means of a JavaScript "select" script.
 * 
 * The "select" script tests each variable in the view's underlying tables and returns <code>true</code> if the variable
 * is to be included in the view.
 */
public class SelectScriptVariablesTabPresenter extends WidgetPresenter<SelectScriptVariablesTabPresenter.Display> {
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
  private EvaluateScriptPresenter scriptWidget;

  private Translations translations = GWT.create(Translations.class);

  private Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

  //
  // Constructors
  //

  @Inject
  public SelectScriptVariablesTabPresenter(final Display display, final EventBus eventBus, EvaluateScriptPresenter scriptWidget) {
    super(display, eventBus);
    this.scriptWidget = scriptWidget;
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
    scriptWidget.bind();
    getDisplay().setScriptWidget(scriptWidget.getDisplay());

    getDisplay().saveChangesEnabled(false);

    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
    scriptWidget.unbind();
  }

  @Override
  public void revealDisplay() {
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  //
  // Methods
  //

  public void setViewDto(ViewDto viewDto) {
    this.viewDto = viewDto;

    viewDto.setFromArray(JsArrays.toSafeArray(viewDto.getFromArray()));

    TableDto tableDto = TableDto.create();
    tableDto.setDatasourceName(viewDto.getDatasourceName());
    tableDto.setName(viewDto.getName());
    scriptWidget.setTable(tableDto);

    JavaScriptViewDto jsViewDto = (JavaScriptViewDto) viewDto.getExtension(JavaScriptViewDto.ViewDtoExtensions.view);
    if(jsViewDto != null && jsViewDto.hasSelect()) {
      getDisplay().setVariablesToView(VariablesToView.SCRIPT);
      getDisplay().setScript(jsViewDto.getSelect());
    } else {
      getDisplay().setVariablesToView(VariablesToView.ALL);
      getDisplay().setScript("");
    }
  }

  private void addEventHandlers() {
    super.registerHandler(eventBus.addHandler(ViewConfigurationRequiredEvent.getType(), new ViewConfigurationRequiredEventHandler()));
    super.registerHandler(getDisplay().addSaveChangesClickHandler(new SaveChangesClickHandler()));
    super.registerHandler(eventBus.addHandler(ViewSavedEvent.getType(), new ViewSavedHandler()));
    super.registerHandler(getDisplay().addVariablestoViewChangeHandler(new VariablesToViewChangeHandler()));
    super.registerHandler(getDisplay().addScriptChangeHandler(new ScriptChangeHandler()));
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
      eventBus.fireEvent(NotificationEvent.newBuilder().error(messages).build());
      return false;
    } else {
      return true;
    }
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {

    Widget getHelpWidget();

    void saveChangesEnabled(boolean enabled);

    void setScriptWidget(EvaluateScriptPresenter.Display scriptWidgetDisplay);

    void setScriptWidgetVisible(boolean visible);

    void setScript(String script);

    String getScript();

    void setVariablesToView(VariablesToView scriptOrAll);

    VariablesToView getVariablesToView();

    HandlerRegistration addSaveChangesClickHandler(ClickHandler clickHandler);

    HandlerRegistration addVariablestoViewChangeHandler(ChangeHandler changeHandler);

    HandlerRegistration addScriptChangeHandler(ChangeHandler changeHandler);

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

      if(getDisplay().getVariablesToView().equals(VariablesToView.SCRIPT)) {
        String script = getDisplay().getScript().trim();
        if(script.length() != 0) {
          jsViewDto.setSelect(script);
        } else {
          jsViewDto.clearSelect();
        }
      } else {
        jsViewDto.clearSelect();
      }

      eventBus.fireEvent(new ViewSaveRequiredEvent(getViewDto()));
    }
  }

  class ViewSavedHandler implements ViewSavedEvent.Handler {

    @Override
    public void onViewSaved(ViewSavedEvent event) {
      getDisplay().saveChangesEnabled(false);
    }
  }

  class VariablesToViewChangeHandler implements ChangeHandler {

    @Override
    public void onChange(ChangeEvent event) {
      if(getDisplay().getVariablesToView() == VariablesToView.ALL) {
        getDisplay().setScript("");
      }
      getDisplay().setScriptWidgetVisible(getDisplay().getVariablesToView().equals(VariablesToView.SCRIPT));
      getDisplay().saveChangesEnabled(true);
      eventBus.fireEvent(new ViewSavePendingEvent());
    }
  }

  class ScriptChangeHandler implements ChangeHandler {

    @Override
    public void onChange(ChangeEvent event) {
      getDisplay().saveChangesEnabled(true);
      eventBus.fireEvent(new ViewSavePendingEvent());
    }
  }
}