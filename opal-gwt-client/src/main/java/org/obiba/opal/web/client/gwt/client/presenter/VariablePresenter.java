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
import org.obiba.opal.web.model.client.CategoryDTO;
import org.obiba.opal.web.model.client.FrequencyDTO;
import org.obiba.opal.web.model.client.VariableDTO;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;

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
        VariableDTO variable = event.getSelection();
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

  private void updateFrequencies(final VariableDTO variable) {
    getDisplay().clearChart();
    ResourceRequest<JsArray<FrequencyDTO>> rr = new ResourceRequest<JsArray<FrequencyDTO>>(variable.getLink() + "/frequencies.json");

    rr.get(new ResourceCallback<JsArray<FrequencyDTO>>() {

      @Override
      public void onResource(JsArray<FrequencyDTO> frequencies) {
        Map<String, FrequencyDTO> freqs = new HashMap<String, FrequencyDTO>();
        for(int i = 0; i < frequencies.length(); i++) {
          FrequencyDTO freq = frequencies.get(i);
          freqs.put(freq.getName(), freq);
        }
        DataTable table = DataTable.create();
        table.addColumn(ColumnType.STRING, "Category");
        table.addColumn(ColumnType.NUMBER, "Frequency");
        JsArray<CategoryDTO> categories = variable.getCategoriesArray();
        for(int i = 0; i < categories.length(); i++) {
          CategoryDTO c = categories.get(i);
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
