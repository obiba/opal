/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.navigator.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.navigator.event.SiblingVariableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.SiblingVariableSelectionEvent.Direction;
import org.obiba.opal.web.gwt.app.client.widgets.event.SummaryRequiredEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.SummaryTabPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 *
 */
public class VariablePresenter extends WidgetPresenter<VariablePresenter.Display> {

  private VariableDto variable;

  @Inject
  private SummaryTabPresenter summaryTabPresenter;

  //
  // Constructors
  //

  /**
   * @param display
   * @param eventBus
   */
  @Inject
  public VariablePresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    super.registerHandler(eventBus.addHandler(VariableSelectionChangeEvent.getType(), new VariableSelectionHandler()));
    summaryTabPresenter.bind();
    getDisplay().setParentCommand(new ParentCommand());
    getDisplay().setNextCommand(new NextCommand());
    getDisplay().setPreviousCommand(new PreviousCommand());
    getDisplay().setSummaryTabCommand(new SummaryCommand());
    getDisplay().setSummaryTabWidget(summaryTabPresenter.getDisplay().asWidget());
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
    summaryTabPresenter.unbind();
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
  }

  //
  // Methods
  //

  private void updateDisplay(VariableDto variableDto, VariableDto previous, VariableDto next) {
    if(variable == null || !isCurrentVariable(variableDto)) {
      variable = variableDto;
      getDisplay().setVariableName(variableDto.getName());
      getDisplay().setEntityType(variableDto.getEntityType());
      getDisplay().setValueType(variableDto.getValueType());
      getDisplay().setMimeType(variableDto.hasMimeType() ? variableDto.getMimeType() : "");
      getDisplay().setUnit(variableDto.hasUnit() ? variableDto.getUnit() : "");
      getDisplay().setRepeatable(variableDto.getIsRepeatable());
      getDisplay().setOccurrenceGroup(variableDto.getIsRepeatable() ? variableDto.getOccurrenceGroup() : "");

      getDisplay().setParentName(variableDto.getParentLink().getRel());
      getDisplay().setPreviousName(previous != null ? previous.getName() : "");
      getDisplay().setNextName(next != null ? next.getName() : "");

      getDisplay().renderCategoryRows(variableDto.getCategoriesArray());
      getDisplay().renderAttributeRows(variableDto.getAttributesArray());
    }
  }

  private boolean isCurrentVariable(VariableDto variableDto) {
    return variableDto.getName().equals(variable.getName()) && variableDto.getParentLink().getRel().equals(variable.getParentLink().getRel());
  }

  /**
   * @param selection
   */
  private void requestSummary(final VariableDto selection) {
    eventBus.fireEvent(new SummaryRequiredEvent(selection.getLink() + "/summary"));
  }

  //
  // Interfaces and classes
  //

  /**
   *
   */
  final class PreviousCommand implements Command {
    @Override
    public void execute() {
      eventBus.fireEvent(new SiblingVariableSelectionEvent(variable, Direction.PREVIOUS));
    }
  }

  /**
   *
   */
  final class NextCommand implements Command {
    @Override
    public void execute() {
      eventBus.fireEvent(new SiblingVariableSelectionEvent(variable, Direction.NEXT));
    }
  }

  /**
   *
   */
  final class ParentCommand implements Command {
    @Override
    public void execute() {
      ResourceRequestBuilderFactory.<TableDto> newBuilder().forResource(variable.getParentLink().getLink()).get().withCallback(new ResourceCallback<TableDto>() {
        @Override
        public void onResource(Response response, TableDto resource) {
          eventBus.fireEvent(new TableSelectionChangeEvent(VariablePresenter.this, resource));
        }

      }).send();
    }
  }

  /**
   *
   */
  final class SummaryCommand implements Command {
    @Override
    public void execute() {
      summaryTabPresenter.refreshDisplay();
    }
  }

  class VariableSelectionHandler implements VariableSelectionChangeEvent.Handler {
    @Override
    public void onVariableSelectionChanged(VariableSelectionChangeEvent event) {
      updateDisplay(event.getSelection(), event.getPrevious(), event.getNext());
      requestSummary(event.getSelection());
      if(getDisplay().isSummaryTabSelected()) {
        summaryTabPresenter.refreshDisplay();
      }
    }
  }

  public interface Display extends WidgetDisplay {

    void setVariableName(String name);

    void setEntityType(String text);

    void setValueType(String text);

    void setMimeType(String text);

    void setUnit(String text);

    void setRepeatable(boolean repeatable);

    void setOccurrenceGroup(String text);

    void setParentName(String name);

    void setPreviousName(String name);

    void setNextName(String name);

    void setParentCommand(Command cmd);

    void setNextCommand(Command cmd);

    void setPreviousCommand(Command cmd);

    void renderCategoryRows(JsArray<CategoryDto> rows);

    void renderAttributeRows(JsArray<AttributeDto> rows);

    void setSummaryTabCommand(Command cmd);

    boolean isSummaryTabSelected();

    void setSummaryTabWidget(Widget widget);
  }
}
