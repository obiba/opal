/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.derive.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.derive.view.ValueMapEntry;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.math.FrequencyDto;
import org.obiba.opal.web.model.client.math.SummaryStatisticsDto;

public class OpenTextualVariableDerivationHelper extends CategoricalVariableDerivationHelper {

  private final Method method;

  public OpenTextualVariableDerivationHelper(VariableDto originalVariable, VariableDto destinationVariable,
      SummaryStatisticsDto summaryStatisticsDto, Method method) {
    super(originalVariable, destinationVariable, summaryStatisticsDto);
    this.method = method;
  }

  @Override
  public void initializeValueMapEntries() {
    valueMapEntries = new ArrayList<ValueMapEntry>();

    if(method == Method.AUTOMATICALLY) {

      for(FrequencyDto frequencyDto : sortByFrequency()) {

        String value = frequencyDto.getValue();
        if(value.equals(NA)) continue;
        ValueMapEntry.Builder entry = ValueMapEntry.fromDistinct(value).count(frequencyDto.getFreq());

        if(estimateIsMissing(value)) {
          entry.missing();
          missingValueMapEntries.add(entry.build());
        } else {
          initializeNonMissingCategoryValueMapEntry(value, entry);
        }
      }
      // recode missing values
      initializeMissingCategoryValueMapEntries();
    }

    valueMapEntries.add(ValueMapEntry.createEmpties(translations.emptyValuesLabel()).count(nbEmpty()).build());
    valueMapEntries.add(ValueMapEntry.createOthers(translations.otherValuesLabel()).build());
  }

  private Iterable<FrequencyDto> sortByFrequency() {
    if (categoricalSummaryDto == null) return Lists.newArrayList();
    List<FrequencyDto> list = JsArrays.toList(categoricalSummaryDto.getFrequenciesArray());
    Collections.sort(list, new Comparator<FrequencyDto>() {
      @Override
      public int compare(FrequencyDto freq1, FrequencyDto freq2) {
        return new Double(freq2.getFreq()).compareTo(freq1.getFreq());
      }
    });
    return list;
  }

  public Method getMethod() {
    return method;
  }

  @Override
  protected DerivedVariableGenerator getDerivedVariableGenerator() {
    return new DerivedOpenTextualVariableGenerator(originalVariable, valueMapEntries);
  }

  public enum Method {

    AUTOMATICALLY, MANUAL;

    public static final String GROUP_METHOD = "group-method";

  }

  public static class DerivedOpenTextualVariableGenerator extends DerivedVariableGenerator {

    public DerivedOpenTextualVariableGenerator(VariableDto originalVariable, List<ValueMapEntry> valueMapEntries) {
      super(originalVariable, valueMapEntries);
    }

    @Override
    protected void generateScript() {
      scriptBuilder.append("$('").append(originalVariable.getName()).append("').map({");
      appendDistinctValueMapEntries();
      scriptBuilder.append("\n  }");
      appendSpecialValuesEntry(getOtherValuesMapEntry());
      appendSpecialValuesEntry(getEmptyValuesMapEntry());
      scriptBuilder.append(");");
    }
  }

}
