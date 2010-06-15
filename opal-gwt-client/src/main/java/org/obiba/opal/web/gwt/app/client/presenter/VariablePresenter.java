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

import org.obiba.opal.web.gwt.app.client.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.model.client.AttributeDto;
import org.obiba.opal.web.model.client.CategoryDto;
import org.obiba.opal.web.model.client.VariableDto;

import com.google.gwt.core.client.JsArray;
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

    void renderCategoryRows(JsArray<CategoryDto> rows);

    void renderAttributeRows(JsArray<AttributeDto> rows);
  }

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
    super.registerHandler(eventBus.addHandler(TableSelectionChangeEvent.getType(), new TableSelectionChangeEvent.Handler() {

      @Override
      public void onNavigatorSelectionChanged(TableSelectionChangeEvent event) {
        getDisplay().getEntityTypeLabel().setText(event.getSelection().getEntityType());
      }
    }));

    super.registerHandler(eventBus.addHandler(VariableSelectionChangeEvent.getType(), new VariableSelectionChangeEvent.Handler() {

      @Override
      public void onVariableSelectionChanged(VariableSelectionChangeEvent event) {
        updateDisplay(event.getSelection());
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

  private void updateDisplay(VariableDto variableDto) {
    getDisplay().getVariableNameLabel().setText(variableDto.getName());
    // getDisplay().getEntityTypeLabel().setText(variableDto.getEntityType());
    getDisplay().getValueTypeLabel().setText(variableDto.getValueType());
    getDisplay().getMimeTypeLabel().setText(variableDto.hasMimeType() ? variableDto.getMimeType() : "");
    // getDisplay().getUnitLabel().setText(variableDto.getUnit());
    getDisplay().getRepeatableLabel().setText(variableDto.getIsRepeatable() ? "Yes" : "No");
    getDisplay().getOccurrenceGroupLabel().setText(variableDto.getIsRepeatable() ? variableDto.getOccurrenceGroup() : "");

    getDisplay().renderCategoryRows(variableDto.getCategoriesArray());
    getDisplay().renderAttributeRows(variableDto.getAttributesArray());
  }
}
