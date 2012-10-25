/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.navigator.view;

import org.obiba.opal.web.gwt.plot.client.FrequencyPlot;
import org.obiba.opal.web.gwt.plot.client.JqPlot;
import org.obiba.opal.web.model.client.math.CategoricalSummaryDto;
import org.obiba.opal.web.model.client.math.FrequencyDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

/**
 *
 */
public class CategoricalSummaryView extends Composite {

  @UiTemplate("CategoricalSummaryView.ui.xml")
  interface CategoricalSummaryViewUiBinder extends UiBinder<HTMLPanel, CategoricalSummaryView> {
  }

  private static CategoricalSummaryViewUiBinder uiBinder = GWT.create(CategoricalSummaryViewUiBinder.class);

  @UiField
  Label obs;

  @UiField
  Label mode;

  @UiField
  DivElement frequencyElement;

  @UiField
  Grid grid;

  @UiField
  DisclosurePanel details;

  final HTMLPanel widget;

  final JqPlot plot;

  public CategoricalSummaryView(CategoricalSummaryDto categorical) {
    widget = uiBinder.createAndBindUi(this);
    initWidget(widget);
    frequencyElement.setId(HTMLPanel.createUniqueId());
    frequencyElement.setAttribute("style", "width:400px;");

    obs.setText("" + Math.round(categorical.getN()));
    mode.setText(categorical.getMode());

    if(categorical.getFrequenciesArray() != null) {
      int count = categorical.getFrequenciesArray().length();
      int width = 400;
      if(count > 10) {
        width = 400 + 20 * (count - 10);
      }
      frequencyElement.setAttribute("style", "width:" + width + "px;");

      FrequencyPlot freqPlot = new FrequencyPlot(frequencyElement.getId());
      grid.resizeRows(count + 1);
      for(int i = 0; i < count; i++) {
        FrequencyDto value = categorical.getFrequenciesArray().get(i);
        if(value.hasValue()) {
          freqPlot.push(value.getValue(), value.getFreq(), value.getPct() * 100);
          grid.setWidget(i + 1, 0, new Label(value.getValue()));
          grid.setWidget(i + 1, 1, new Label("" + Math.round(value.getFreq())));
          grid.setWidget(i + 1, 2, new Label("" + value.getPct() * 100));
        }
      }
      this.plot = freqPlot;

      details.setVisible(true);

    } else {
      this.plot = null;
      details.setVisible(false);
    }
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    if(plot != null) {
      plot.plotOrRedraw();
    }
  }

}
