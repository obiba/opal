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

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.CategoryDto;
import org.obiba.opal.web.model.client.FrequencyDto;
import org.obiba.opal.web.model.client.VariableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
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

  final private ResourceRequestBuilderFactory factory;

  /**
   * @param display
   * @param eventBus
   */
  @Inject
  public VariablePresenter(Display display, EventBus eventBus, final ResourceRequestBuilderFactory factory) {
    super(display, eventBus);
    this.factory = factory;
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    super.registerHandler(eventBus.addHandler(VariableSelectionChangeEvent.getType(), new VariableSelectionChangeEvent.Handler() {
      @Override
      public void onVariableSelectionChanged(VariableSelectionChangeEvent event) {
        VariableDto variable = event.getSelection();
        updateFrequencies(variable);
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

  private void updateFrequencies(final VariableDto variable) {
    getDisplay().clearChart();

    factory.<JsArray<FrequencyDto>> newBuilder().forResource(variable.getLink() + "/frequencies.json").get().withCallback(new ResourceCallback<JsArray<FrequencyDto>>() {

      @Override
      public void onResource(Response response, JsArray<FrequencyDto> frequencies) {
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
