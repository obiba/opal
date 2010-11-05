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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewUpdateEvent;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.EvaluateScriptPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.EvaluateScriptPresenter.Mode;
import org.obiba.opal.web.model.client.magma.JavaScriptViewDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
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
    scriptWidget.setEvaluationMode(Mode.VARIABLE);
    getDisplay().setScriptWidget(scriptWidget.getDisplay());

    getDisplay().saveChangesEnabled(true);

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

    TableDto tableDto = TableDto.create();
    tableDto.setDatasourceName(viewDto.getDatasourceName());
    tableDto.setName(viewDto.getName());
    scriptWidget.setTable(tableDto);

    JavaScriptViewDto jsViewDto = (JavaScriptViewDto) viewDto.getExtension(JavaScriptViewDto.ViewDtoExtensions.view);
    if(jsViewDto.hasSelect()) {
      getDisplay().setVariablesToView(VariablesToView.SCRIPT);
      getDisplay().setScript(jsViewDto.getSelect());
    } else {
      getDisplay().setVariablesToView(VariablesToView.ALL);
      getDisplay().setScript("");
    }
  }

  private void addEventHandlers() {
    super.registerHandler(getDisplay().addSaveChangesClickHandler(new SaveChangesClickHandler()));
    super.registerHandler(getDisplay().addVariablestoViewChangeHandler(new VariablesToViewChangeHandler()));
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {

    void saveChangesEnabled(boolean enabled);

    void setScriptWidget(EvaluateScriptPresenter.Display scriptWidgetDisplay);

    void setScriptWidgetVisible(boolean visible);

    void setScript(String script);

    String getScript();

    void setVariablesToView(VariablesToView scriptOrAll);

    VariablesToView getVariablesToView();

    HandlerRegistration addSaveChangesClickHandler(ClickHandler clickHandler);

    HandlerRegistration addVariablestoViewChangeHandler(ChangeHandler changeHandler);
  }

  public enum VariablesToView {
    SCRIPT, ALL
  }

  class SaveChangesClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      updateViewDto();
      eventBus.fireEvent(new ViewUpdateEvent(getViewDto()));
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
    }
  }

  class VariablesToViewChangeHandler implements ChangeHandler {

    @Override
    public void onChange(ChangeEvent event) {
      getDisplay().setScriptWidgetVisible(getDisplay().getVariablesToView().equals(VariablesToView.SCRIPT));
    }
  }
}