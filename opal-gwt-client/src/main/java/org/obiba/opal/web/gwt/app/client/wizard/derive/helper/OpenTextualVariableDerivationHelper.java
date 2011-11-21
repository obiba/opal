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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry.Builder;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.math.CategoricalSummaryDto;
import org.obiba.opal.web.model.client.math.FrequencyDto;
import org.obiba.opal.web.model.client.math.SummaryStatisticsDto;

import com.google.gwt.core.client.JsArray;

public class OpenTextualVariableDerivationHelper extends CategoricalVariableDerivationHelper {

  private Method method;

  public OpenTextualVariableDerivationHelper(VariableDto originalVariable, SummaryStatisticsDto summaryStatisticsDto, Method method) {
    super(originalVariable, summaryStatisticsDto);
    this.method = method;
  }

  @Override
  public void initializeValueMapEntries() {
    this.valueMapEntries = new ArrayList<ValueMapEntry>();
    valueMapEntries.add(ValueMapEntry.createEmpties(translations.emptyValuesLabel()).build());
    valueMapEntries.add(ValueMapEntry.createOthers(translations.otherValuesLabel()).build());

    if(method == Method.AUTOMATICALLY) {

      List<ValueMapEntry> missingValueMapEntries = new ArrayList<ValueMapEntry>();
      int index = 1;
      for(FrequencyDto frequencyDto : sortByFrequency()) {

        Builder entry = ValueMapEntry.fromDistinct(frequencyDto.getValue()).label(frequencyDto.getValue()).count(frequencyDto.getFreq());

        if(estimateIsMissing(frequencyDto.getValue())) {
          entry.missing();
          missingValueMapEntries.add(entry.build());
        } else {
          index = initializeNonMissingCategoryValueMapEntry(index, frequencyDto.getValue(), entry);
        }
      }

      // recode missing values
      initializeMissingCategoryValueMapEntries(missingValueMapEntries, index);

    }
  }

  private List<FrequencyDto> sortByFrequency() {
	// TODO do not recast.
    CategoricalSummaryDto categoricalSummaryDto = statisticsDto.getExtension(CategoricalSummaryDto.SummaryStatisticsDtoExtensions.categorical).cast();
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

  // TODO this has been ~ copy/paste from temporal helper (maybe we can factorize)
  @Override
  public VariableDto getDerivedVariable() {
    VariableDto derived = copyVariable(originalVariable);
    derived.setValueType("text");

    Map<String, CategoryDto> newCategoriesMap = new LinkedHashMap<String, CategoryDto>();

    StringBuilder scriptBuilder = new StringBuilder("$('" + originalVariable.getName() + "')");
    scriptBuilder.append(".map({");
    appendDistinctValueMapEntries(scriptBuilder, newCategoriesMap);
    scriptBuilder.append("\n").append("  }");
    appendSpecialValuesEntry(scriptBuilder, newCategoriesMap, getOtherValuesMapEntry());
    appendSpecialValuesEntry(scriptBuilder, newCategoriesMap, getEmptyValuesMapEntry());
    scriptBuilder.append(");");

    setScript(derived, scriptBuilder.toString());

    // new categories
    JsArray<CategoryDto> cats = JsArrays.create();
    for(CategoryDto cat : newCategoriesMap.values()) {
      cats.push(cat);
    }
    derived.setCategoriesArray(cats);

    return derived;
  }

  public enum Method {

    AUTOMATICALLY("Automatically"), MANUAL("Manual");

    private final String method;

    public static final String group = "group-method";

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
  }
}
