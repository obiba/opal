/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.client.gwt.client.presenter;

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.client.gwt.client.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.client.gwt.client.event.VariableSelectionChangeEventHandler;
import org.obiba.opal.web.client.gwt.client.rest.ResourceCallback;
import org.obiba.opal.web.client.gwt.client.rest.ResourceRequest;
import org.obiba.opal.web.model.client.CategoryDto;
import org.obiba.opal.web.model.client.FrequencyDto;
import org.obiba.opal.web.model.client.VariableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.inject.Inject;

/**
 *
 */
public class VariablePresenter extends WidgetPresenter<VariablePresenter.Display> {

  public interface Display extends WidgetDisplay {

    public void renderData(AbstractDataTable data);

    public void clearChart();

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
    eventBus.addHandler(VariableSelectionChangeEvent.getType(), new VariableSelectionChangeEventHandler() {
      @Override
      public void onVariableSelectionChanged(VariableSelectionChangeEvent event) {
        VariableDto variable = event.getSelection();
        updateFrequencies(variable);
      }
    });
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

  private void updateFrequencies(final VariableDto variable) {
    getDisplay().clearChart();
    ResourceRequest<JsArray<FrequencyDto>> rr = new ResourceRequest<JsArray<FrequencyDto>>(variable.getLink() + "/frequencies.json");

    rr.get(new ResourceCallback<JsArray<FrequencyDto>>() {

      @Override
      public void onResource(JsArray<FrequencyDto> frequencies) {
        Map<String, FrequencyDto> freqs = new HashMap<String, FrequencyDto>();
        for(int i = 0; i < frequencies.length(); i++) {
          FrequencyDto freq = frequencies.get(i);
          freqs.put(freq.getName(), freq);
        }
        DataTable table = DataTable.create();
        table.addColumn(ColumnType.STRING, "Category");
        table.addColumn(ColumnType.NUMBER, "Frequency");
        JsArray<CategoryDto> categories = variable.getCategoriesArray();
        for(int i = 0; i < categories.length(); i++) {
          CategoryDto c = categories.get(i);
          int row = table.addRow();
          table.setValue(row, 0, c.getName());
          table.setValue(row, 1, freqs.get(c.getName()).getValue());
        }
        int row = table.addRow();
        table.setValue(row, 0, "N/A");
        table.setValue(row, 1, freqs.get("N/A").getValue());
        getDisplay().renderData(table);
      }
    });
  }
}
