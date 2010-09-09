/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.SiblingVariableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.event.SiblingVariableSelectionEvent.Direction;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.math.SummaryStatisticsDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.inject.Inject;

/**
 *
 */
public class VariablePresenter extends WidgetPresenter<VariablePresenter.Display> {

  private VariableDto variable;

  private SummaryStatisticsDto summary;

  private Request summaryRequest;

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

    getDisplay().setParentCommand(new ParentCommand());
    getDisplay().setNextCommand(new NextCommand());
    getDisplay().setPreviousCommand(new PreviousCommand());
    getDisplay().setSummaryTabCommand(new SummaryCommand());
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
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
    getDisplay().requestingSummary();
    summaryRequest = ResourceRequestBuilderFactory.<SummaryStatisticsDto> newBuilder().forResource(variable.getLink() + "/summary").get().withCallback(new ResourceCallback<SummaryStatisticsDto>() {
      @Override
      public void onResource(Response response, SummaryStatisticsDto resource) {
        if(isCurrentVariable(selection)) {
          summary = resource;
          getDisplay().renderSummary(resource);
        }
      }

    }).send();
  }

  private void cancelPendingSummaryRequest() {
    summary = null;
    if(summaryRequest != null && summaryRequest.isPending()) {
      summaryRequest.cancel();
      summaryRequest = null;
    }
  }

  private boolean hasSummaryOrPendingRequest() {
    return summary != null || (summaryRequest != null && summaryRequest.isPending());
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
      if(hasSummaryOrPendingRequest() == false) {
        requestSummary(variable);
      }
    }
  }

  class VariableSelectionHandler implements VariableSelectionChangeEvent.Handler {
    @Override
    public void onVariableSelectionChanged(VariableSelectionChangeEvent event) {
      cancelPendingSummaryRequest();
      updateDisplay(event.getSelection(), event.getPrevious(), event.getNext());
      if(getDisplay().isSummaryTabSelected()) {
        requestSummary(event.getSelection());
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

    void requestingSummary();

    void renderSummary(SummaryStatisticsDto summary);
  }
}
