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
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;

/**
 *
 */
public class VariablePresenter extends WidgetPresenter<VariablePresenter.Display> {

  public interface Display extends WidgetDisplay {

    HasText getVariableNameLabel();

    HasText getEntityTypeLabel();

    HasText getValueTypeLabel();

    HasText getMimeTypeLabel();

    HasText getUnitLabel();

    HasText getRepeatableLabel();

    HasText getOccurrenceGroupLabel();

    HasText getParentName();

    HasClickHandlers getParentLink();

    HasClickHandlers getPreviousLink();

    HasClickHandlers getNextLink();

    void renderCategoryRows(JsArray<CategoryDto> rows);

    void renderAttributeRows(JsArray<AttributeDto> rows);
  }

  private static Translations translations = GWT.create(Translations.class);

  private VariableDto variable;

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
    registerVariableSelectionChangeHandler();

    super.registerHandler(getDisplay().getParentLink().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        ResourceRequestBuilderFactory.<TableDto> newBuilder().forResource(variable.getParentLink().getLink()).get().withCallback(new ResourceCallback<TableDto>() {
          @Override
          public void onResource(Response response, TableDto resource) {
            eventBus.fireEvent(new TableSelectionChangeEvent(resource));
          }

        }).send();
      }
    }));

    super.registerHandler(getDisplay().getNextLink().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        eventBus.fireEvent(new SiblingVariableSelectionEvent(variable, Direction.NEXT));
      }
    }));

    super.registerHandler(getDisplay().getPreviousLink().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        eventBus.fireEvent(new SiblingVariableSelectionEvent(variable, Direction.PREVIOUS));
      }
    }));

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

  private void registerVariableSelectionChangeHandler() {
    super.registerHandler(eventBus.addHandler(VariableSelectionChangeEvent.getType(), new VariableSelectionChangeEvent.Handler() {

      @Override
      public void onVariableSelectionChanged(VariableSelectionChangeEvent event) {
        updateDisplay(event.getSelection());
      }
    }));
  }

  private void updateDisplay(VariableDto variableDto) {
    if(!variableDto.getName().equals(variable.getName()) || !variableDto.getParentLink().getRel().equals(variable.getParentLink().getRel())) {
      variable = variableDto;
      getDisplay().getVariableNameLabel().setText(variableDto.getName());
      getDisplay().getEntityTypeLabel().setText(variableDto.getEntityType());
      getDisplay().getValueTypeLabel().setText(variableDto.getValueType());
      getDisplay().getMimeTypeLabel().setText(variableDto.hasMimeType() ? variableDto.getMimeType() : "");
      getDisplay().getUnitLabel().setText(variableDto.hasUnit() ? variableDto.getUnit() : "");
      getDisplay().getRepeatableLabel().setText(variableDto.getIsRepeatable() ? translations.yesLabel() : translations.noLabel());
      getDisplay().getOccurrenceGroupLabel().setText(variableDto.getIsRepeatable() ? variableDto.getOccurrenceGroup() : "");

      getDisplay().getParentName().setText(variableDto.getParentLink().getRel());

      getDisplay().renderCategoryRows(variableDto.getCategoriesArray());
      getDisplay().renderAttributeRows(variableDto.getAttributesArray());
    }
  }
}
