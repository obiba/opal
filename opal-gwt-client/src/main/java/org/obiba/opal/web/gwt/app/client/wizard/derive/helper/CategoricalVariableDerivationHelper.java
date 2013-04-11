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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.math.CategoricalSummaryDto;
import org.obiba.opal.web.model.client.math.FrequencyDto;
import org.obiba.opal.web.model.client.math.SummaryStatisticsDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.regexp.shared.RegExp;

/**
 *
 */
public class CategoricalVariableDerivationHelper extends DerivationHelper {

  private static final String MISSING_REGEXP = "^DNK$|^DK-NA$|^PNA$|^REFUSED$";

  private static final String NONE_REGEXP = "^NONE$|^NEVER$";

  private static final String YES_REGEXP = "^Y$|^YES$|^TRUE$";

  private static final String NO_REGEXP = "^N$|^NO$|^FALSE$";

  private static final String MALE_REGEXP = "^MALE$";

  private static final String FEMALE_REGEXP = "^FEMALE$";

  protected final CategoricalSummaryDto categoricalSummaryDto;

  private double maxFrequency;

  private List<String> destinationCategories;

  public CategoricalVariableDerivationHelper(VariableDto originalVariable) {
    this(originalVariable, null, null);
  }

  public CategoricalVariableDerivationHelper(VariableDto originalVariable, @Nullable VariableDto destination,
      @Nullable SummaryStatisticsDto summaryStatisticsDto) {
    super(originalVariable, destination);
    //noinspection RedundantCast
    categoricalSummaryDto = summaryStatisticsDto == null
        ? null
        : (CategoricalSummaryDto) summaryStatisticsDto
            .getExtension(CategoricalSummaryDto.SummaryStatisticsDtoExtensions.categorical).cast();
  }

  @Override
  public void initializeValueMapEntries() {
    initializeValueMapEntries(true);
  }

  public void initializeValueMapEntries(boolean recodeCategoriesName) {
    valueMapEntries = new ArrayList<ValueMapEntry>();

    destinationCategories = getDestinationCategories(getDestination());

    Collection<ValueMapEntry> missingValueMapEntries = new ArrayList<ValueMapEntry>();
    int index = 1;

    // For each category and value without category, make value map entry and process separately missing and non-missing
    // ones.
    Map<String, Double> countByCategoryName = new HashMap<String, Double>();
    if(isSummaryAvailable()) {
      findFrequencies(countByCategoryName, categoricalSummaryDto.getFrequenciesArray());
    }

    // recode categories
    index = initializeNonMissingCategoryValueMapEntries(countByCategoryName, missingValueMapEntries, index,
        recodeCategoriesName);

    // recode values not corresponding to a category
    index = initializeValueMapEntriesWithoutCategory(countByCategoryName, missingValueMapEntries, index,
        recodeCategoriesName);

    // recode missing values
    initializeMissingCategoryValueMapEntries(missingValueMapEntries, index, recodeCategoriesName);

    valueMapEntries.add(ValueMapEntry.createEmpties(translations.emptyValuesLabel()).count(nbEmpty()).build());
    valueMapEntries.add(ValueMapEntry.createOthers(translations.otherValuesLabel()).build());

  }

  private boolean isSummaryAvailable() {
    return categoricalSummaryDto != null;
  }

  private int initializeValueMapEntriesWithoutCategory(Map<String, Double> countByCategoryName,
      Collection<ValueMapEntry> missingValueMapEntries, int index, boolean recodeCategoriesName) {
    int newIndex = index;
    for(Map.Entry<String, Double> entry : countByCategoryName.entrySet()) {
      String value = entry.getKey();
      ValueMapEntry.Builder builder = ValueMapEntry.fromDistinct(value, entry.getValue());
      if(estimateIsMissing(value)) {
        builder.missing();
        missingValueMapEntries.add(builder.build());
      } else {
        newIndex = initializeNonMissingCategoryValueMapEntry(newIndex, value, builder, recodeCategoriesName);
      }
    }
    return newIndex;
  }

  private int initializeNonMissingCategoryValueMapEntries(Map<String, Double> countByCategoryName,
      Collection<ValueMapEntry> missingValueMapEntries, int index, boolean recodeCategoriesName) {
    int newIndex = index;
    for(CategoryDto category : JsArrays.toIterable(originalVariable.getCategoriesArray())) {
      double count = countByCategoryName.containsKey(category.getName())
          ? countByCategoryName.get(category.getName())
          : 0;
      ValueMapEntry.Builder builder = ValueMapEntry.fromCategory(category, count);
      if(estimateIsMissing(category)) {
        builder.missing();
        missingValueMapEntries.add(builder.build());
      } else {
        newIndex = initializeNonMissingCategoryValueMapEntry(newIndex, category.getName(), builder,
            recodeCategoriesName);
      }
      countByCategoryName.remove(category.getName());
    }
    return newIndex;
  }

  private void findFrequencies(Map<String, Double> countByCategoryName, JsArray<FrequencyDto> frequencies) {
    if(frequencies == null) return;
    for(int i = 0; i < frequencies.length(); i++) {
      FrequencyDto frequencyDto = frequencies.get(i);
      String value = frequencyDto.getValue();
      if(value.equals(NA)) continue;
      countByCategoryName.put(frequencyDto.getValue(), frequencyDto.getFreq());
      if(frequencyDto.getFreq() > maxFrequency) {
        maxFrequency = frequencyDto.getFreq();
      }
    }
  }

  public double getMaxFrequency() {
    return maxFrequency;
  }

  /**
   * Return frequency of N/A in summary stats
   */
  protected double nbEmpty() {
    if(isSummaryAvailable()) {
      JsArray<FrequencyDto> frequenciesArray = categoricalSummaryDto.getFrequenciesArray();
      if(frequenciesArray != null) {
        for(int i = 0; i < frequenciesArray.length(); i++) {
          if(frequenciesArray.get(i).getValue().equals(NA)) {
            return frequenciesArray.get(i).getFreq();
          }
        }
      }
    }
    return 0;
  }

  /**
   * Process non-missing categories.
   *
   * @param index
   * @param value
   * @param builder
   * @return current index value
   */
  protected int initializeNonMissingCategoryValueMapEntry(int index, String value, ValueMapEntry.Builder builder) {
    return initializeNonMissingCategoryValueMapEntry(index, value, builder, false);
  }

  @SuppressWarnings({ "PMD.NcssMethodCount", "OverlyLongMethod" })
  protected int initializeNonMissingCategoryValueMapEntry(int index, String value, ValueMapEntry.Builder builder,
      boolean recodeCategoriesName) {
    int newIndex = index;

    if(recodeCategoriesName) {
      //noinspection IfStatementWithTooManyBranches
      if(RegExp.compile("^\\d+$").test(value)) {
        builder.newValue(value);
      } else if(RegExp.compile(NO_REGEXP + "|" + NONE_REGEXP, "i").test(value)) {
        builder.newValue("0");
      } else if(RegExp.compile(YES_REGEXP + "|" + MALE_REGEXP, "i").test(value)) {
        builder.newValue("1");
        if(index < 2) newIndex = 2;
      } else if(RegExp.compile(FEMALE_REGEXP, "i").test(value)) {
        builder.newValue("2");
        if(index < 3) newIndex = 3;
      } else {
        // OPAL-1387 look for a similar entry value and apply same new value
        boolean found = false;
        for(ValueMapEntry entry : valueMapEntries) {
          if(entry.getValue().trim().compareToIgnoreCase(value.trim()) == 0) {
            builder.newValue(entry.getNewValue());
            found = true;
            break;
          }
        }
        // create new value only if we don't already have mapped all existing categories in destination variable
        if(!found && (destinationCategories == null || newIndex <= destinationCategories.size())) {
          builder.newValue(Integer.toString(newIndex++));
        }
      }
    } else {
      builder.newValue(value);
    }

    valueMapEntries.add(builder.build());

    return newIndex;
  }

  /**
   * Recode each missing values with indexes like 8s and 9s.
   *
   * @param missingValueMapEntries
   * @param indexMax
   */
  protected void initializeMissingCategoryValueMapEntries(Collection<ValueMapEntry> missingValueMapEntries,
      int indexMax) {
    initializeMissingCategoryValueMapEntries(missingValueMapEntries, indexMax, false);
  }

  protected void initializeMissingCategoryValueMapEntries(Collection<ValueMapEntry> missingValueMapEntries,
      int indexMax, boolean recodeCategoriesName) {
    if(missingValueMapEntries.isEmpty()) return;

    if(recodeCategoriesName) {
      int missIndex = 10 - missingValueMapEntries.size();
      int factor = 1;
      while(missIndex * factor < indexMax + 1) {
        factor = factor * 10 + 1;
      }
      for(ValueMapEntry entry : missingValueMapEntries) {
        entry.setNewValue(Integer.toString(missIndex * factor));
        missIndex++;
      }
    } else {
      for(ValueMapEntry entry : missingValueMapEntries) {
        entry.setNewValue(entry.getValue());
      }
    }
    valueMapEntries.addAll(missingValueMapEntries);
  }

  @Override
  protected DerivedVariableGenerator getDerivedVariableGenerator() {
    return new DerivedCategoricalVariableGenerator(originalVariable, valueMapEntries);
  }

  private boolean estimateIsMissing(CategoryDto cat) {
    return estimateIsMissing(cat.getName()) || cat.hasIsMissing() && cat.getIsMissing();
  }

  protected boolean estimateIsMissing(String value) {
    return Strings.isNullOrEmpty(value) || RegExp.compile(MISSING_REGEXP, "i").test(value);
  }

  public List<String> getDestinationCategories() {
    return destinationCategories;
  }

  public static class DerivedCategoricalVariableGenerator extends DerivedVariableGenerator {

    public DerivedCategoricalVariableGenerator(VariableDto originalVariable, List<ValueMapEntry> valueMapEntries) {
      super(originalVariable, valueMapEntries);
    }

    @Override
    protected void generateScript() {
      scriptBuilder.append("$('").append(originalVariable.getName()).append("').map({");
      appendCategoryValueMapEntries();
      appendDistinctValueMapEntries();
      scriptBuilder.append("\n  }");
      appendSpecialValuesEntry(getOtherValuesMapEntry());
      appendSpecialValuesEntry(getEmptyValuesMapEntry());
      scriptBuilder.append(");");
    }
  }
}
