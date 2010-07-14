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
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.AttributeDto;
import org.obiba.opal.web.model.client.CategoryDto;
import org.obiba.opal.web.model.client.TableDto;
import org.obiba.opal.web.model.client.VariableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

/**
 *
 */
public class VariablePresenter extends WidgetPresenter<VariablePresenter.Display> {

  private static Translations translations = GWT.create(Translations.class);

  private VariableDto variable;

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
    super.registerHandler(getDisplay().addParentLinkClickHandler(new ParentClickHandler()));
    super.registerHandler(getDisplay().addParentImageClickHandler(new ParentClickHandler()));
    super.registerHandler(getDisplay().addNextClickHandler(new NextClickHandler()));
    super.registerHandler(getDisplay().addPreviousClickHandler(new PreviousClickHandler()));
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
      getDisplay().setRepeatable(variableDto.getIsRepeatable() ? translations.yesLabel() : translations.noLabel());
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

  //
  // Interfaces and classes
  //

  class VariableSelectionHandler implements VariableSelectionChangeEvent.Handler {
    @Override
    public void onVariableSelectionChanged(VariableSelectionChangeEvent event) {
      updateDisplay(event.getSelection(), event.getPrevious(), event.getNext());
    }
  }

  class PreviousClickHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent event) {
      eventBus.fireEvent(new SiblingVariableSelectionEvent(variable, Direction.PREVIOUS));
    }
  }

  class NextClickHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent event) {
      eventBus.fireEvent(new SiblingVariableSelectionEvent(variable, Direction.NEXT));
    }
  }

  class ParentClickHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent event) {
      ResourceRequestBuilderFactory.<TableDto> newBuilder().forResource(variable.getParentLink().getLink()).get().withCallback(new ResourceCallback<TableDto>() {
        @Override
        public void onResource(Response response, TableDto resource) {
          eventBus.fireEvent(new TableSelectionChangeEvent(VariablePresenter.this, resource));
        }

      }).send();
    }
  }

  public interface Display extends WidgetDisplay {

    void setVariableName(String name);

    void setEntityType(String text);

    void setValueType(String text);

    void setMimeType(String text);

    void setUnit(String text);

    void setRepeatable(String text);

    void setOccurrenceGroup(String text);

    void setParentName(String name);

    void setPreviousName(String name);

    void setNextName(String name);

    HandlerRegistration addParentLinkClickHandler(ClickHandler handler);

    HandlerRegistration addParentImageClickHandler(ClickHandler handler);

    HandlerRegistration addNextClickHandler(ClickHandler handler);

    HandlerRegistration addPreviousClickHandler(ClickHandler handler);

    void renderCategoryRows(JsArray<CategoryDto> rows);

    void renderAttributeRows(JsArray<AttributeDto> rows);
  }
}
