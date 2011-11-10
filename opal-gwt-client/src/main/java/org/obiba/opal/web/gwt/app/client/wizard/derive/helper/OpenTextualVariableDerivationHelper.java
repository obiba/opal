/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.derive.helper;

import java.util.ArrayList;

import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveOpenTextualVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveOpenTextualVariableStepPresenter.Display;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.math.CategoricalSummaryDto;
import org.obiba.opal.web.model.client.math.FrequencyDto;
import org.obiba.opal.web.model.client.math.SummaryStatisticsDto;

import com.google.gwt.http.client.Response;

/**
 *
 */
public class OpenTextualVariableDerivationHelper extends VariableDuplicationHelper {

  private final DeriveOpenTextualVariableStepPresenter.Display display;

  private Method method;

  public OpenTextualVariableDerivationHelper(VariableDto originalVariable, Display display) {
    super(originalVariable);
    this.display = display;
    this.method = display.getMethod();
    initializeValueMapEntries();
  }

  @Override
  protected void initializeValueMapEntries() {
    this.valueMapEntries = new ArrayList<ValueMapEntry>();

    if(method.isAutomatically()) {

      valueMapEntries.add(ValueMapEntry.createEmpties(translations.emptyValuesLabel()).build());
      valueMapEntries.add(ValueMapEntry.createOthers(translations.otherValuesLabel()).build());

      StringBuilder link = new StringBuilder(originalVariable.getLink())//
      .append("/summary")//
      .append("?nature=CATEGORICAL")//
      .append("&distinct=TRUE");

      ResourceRequestBuilderFactory.<SummaryStatisticsDto> newBuilder()//
      .forResource(link.toString()).get()//
      .withCallback(new ResourceCallback<SummaryStatisticsDto>() {
        @Override
        public void onResource(Response response, SummaryStatisticsDto dto) {
          CategoricalSummaryDto categoricalSummaryDto = dto.getExtension(CategoricalSummaryDto.SummaryStatisticsDtoExtensions.categorical).cast();
          for(int i = 0; i < categoricalSummaryDto.getFrequenciesArray().length(); i++) {
            FrequencyDto frequencyDto = categoricalSummaryDto.getFrequenciesArray().get(i);
            valueMapEntries.add(ValueMapEntry.fromDistinct(frequencyDto.getValue()).build());
          }
          display.populateValues(valueMapEntries);
        }
      }).send();
    } else {
      display.populateValues(new ArrayList<ValueMapEntry>());
    }
  }

  public Method getMethod() {
    return method;
  }

  public boolean addEntry(String value, String newValue) {
    if(value != null && !value.trim().equals("") && newValue != null && !newValue.trim().equals("")) {
      valueMapEntries.add(ValueMapEntry.fromDistinct(value).newValue(newValue).build());
      return true;
    }
    return false;
  }

  public enum Method {

    AUTOMATICALLY("Automatically"), MANUAL("Manual");

    private final String method;

    Method(String method) {
      this.method = method;
    }

    public static Method fromString(String text) {
      for(Method g : values()) {
        if(g.method.equalsIgnoreCase(text)) {
          return g;
        }
      }
      return null;
    }

    @Override
    public String toString() {
      return method;
    }

    public boolean isAutomatically() {
      return this == AUTOMATICALLY;
    }
  }

}
