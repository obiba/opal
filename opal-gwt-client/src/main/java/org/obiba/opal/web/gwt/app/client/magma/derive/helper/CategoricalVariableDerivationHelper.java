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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.derive.view.ValueMapEntry;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.math.CategoricalSummaryDto;
import org.obiba.opal.web.model.client.math.FrequencyDto;
import org.obiba.opal.web.model.client.math.SummaryStatisticsDto;

import com.google.common.base.Strings;
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

  private final boolean recodeCategoriesName;

  private double maxFrequency;

  private List<String> destinationCategories;

  private final Map<String, Double> countByCategoryName = new HashMap<String, Double>();

  protected final Collection<ValueMapEntry> missingValueMapEntries = new ArrayList<ValueMapEntry>();

  protected int index = 1;

  private int valueAt = -1;

  public CategoricalVariableDerivationHelper(VariableDto originalVariable) {
    this(null, originalVariable, null, null, true, -1);
  }

  public CategoricalVariableDerivationHelper(VariableDto originalVariable, int valueAt) {
    this(null, originalVariable, null, null, true, valueAt);
  }

  public CategoricalVariableDerivationHelper(String originalTableReference, VariableDto originalVariable, int valueAt) {
    this(originalTableReference, originalVariable, null, null, true, valueAt);
  }

  public CategoricalVariableDerivationHelper(VariableDto originalVariable, boolean recodeCategoriesName) {
    this(null, originalVariable, null, null, recodeCategoriesName, -1);
  }

  public CategoricalVariableDerivationHelper(VariableDto originalVariable, @Nullable VariableDto destination,
      @Nullable SummaryStatisticsDto summaryStatisticsDto) {
    this(null, originalVariable, destination, summaryStatisticsDto, true, -1);
  }

  private CategoricalVariableDerivationHelper(String originalTableReference, VariableDto originalVariable, @Nullable VariableDto destination,
                                              @Nullable SummaryStatisticsDto summaryStatisticsDto, boolean recodeCategoriesName, int valueAt) {
    super(originalTableReference, originalVariable, destination);
    this.recodeCategoriesName = recodeCategoriesName;
    this.valueAt = valueAt;
    //noinspection RedundantCast
    categoricalSummaryDto = summaryStatisticsDto == null
        ? null
        : (CategoricalSummaryDto) summaryStatisticsDto
            .getExtension(CategoricalSummaryDto.SummaryStatisticsDtoExtensions.categorical).cast();
  }

  @Override
  public void initializeValueMapEntries() {
    valueMapEntries = new ArrayList<ValueMapEntry>();

    destinationCategories = getDestinationCategories(getDestination());

    // For each category and value without destination category,
    // make value map entry and process separately missing and non-missing ones.
    loadCountByCategoryName();

    // recode categories
    initializeNonMissingCategoryValueMapEntries();

    // recode values not corresponding to a category
    initializeValueMapEntriesWithoutCategory();

    // recode missing values
    initializeMissingCategoryValueMapEntries();

    valueMapEntries.add(ValueMapEntry.createEmpties(translations.emptyValuesLabel()).count(nbEmpty()).build());
    valueMapEntries.add(ValueMapEntry.createOthers(translations.otherValuesLabel()).build());
  }

  private void initializeValueMapEntriesWithoutCategory() {
    for(Map.Entry<String, Double> entry : countByCategoryName.entrySet()) {
      String value = entry.getKey();
      ValueMapEntry.Builder builder = ValueMapEntry.fromDistinct(value, entry.getValue());
      if(estimateIsMissing(value)) {
        builder.missing();
        missingValueMapEntries.add(builder.build());
      } else {
        initializeNonMissingCategoryValueMapEntry(value, builder);
      }
    }
  }

  private void initializeNonMissingCategoryValueMapEntries() {
    for(CategoryDto category : JsArrays.toIterable(originalVariable.getCategoriesArray())) {
      String categoryName = category.getName();
      double count = countByCategoryName.containsKey(categoryName) ? countByCategoryName.get(categoryName) : 0;
      ValueMapEntry.Builder builder = ValueMapEntry.fromCategory(category, count);
      if(estimateIsMissing(category)) {
        builder.missing();
        missingValueMapEntries.add(builder.build());
      } else {
        initializeNonMissingCategoryValueMapEntry(categoryName, builder);
      }
      countByCategoryName.remove(categoryName);
    }
  }

  private void loadCountByCategoryName() {
    if(categoricalSummaryDto == null) return;
    for(FrequencyDto frequencyDto : JsArrays.toIterable(categoricalSummaryDto.getFrequenciesArray())) {
      String value = frequencyDto.getValue();
      if(value.equals(NA)) continue;
      double freq = frequencyDto.getFreq();
      countByCategoryName.put(value, freq);
      if(freq > maxFrequency) {
        maxFrequency = freq;
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
    if(categoricalSummaryDto != null) {
      for(FrequencyDto frequencyDto : JsArrays.toIterable(categoricalSummaryDto.getFrequenciesArray())) {
        if(frequencyDto.getValue().equals(NA)) {
          return frequencyDto.getFreq();
        }
      }
    }
    return 0;
  }

  /**
   * Process non-missing categories.
   */
  @SuppressWarnings({ "PMD.NcssMethodCount", "OverlyLongMethod" })
  protected void initializeNonMissingCategoryValueMapEntry(String value, ValueMapEntry.Builder builder) {

    // don't overwrite destination categories if they already exist
    if(getDestination() == null) {
      if(recodeCategoriesName) {
        //noinspection IfStatementWithTooManyBranches
        if(RegExp.compile("^\\d+$").test(value)) {
          builder.newValue(value);
        } else if(RegExp.compile(NO_REGEXP + "|" + NONE_REGEXP, "i").test(value)) {
          builder.newValue("0");
        } else if(RegExp.compile(YES_REGEXP + "|" + MALE_REGEXP, "i").test(value)) {
          builder.newValue("1");
          if(index < 2) index = 2;
        } else if(RegExp.compile(FEMALE_REGEXP, "i").test(value)) {
          builder.newValue("2");
          if(index < 3) index = 3;
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
          if(!found) {
            builder.newValue(Integer.toString(index++));
          }
        }
      } else {
        builder.newValue(value);
      }
    }

    valueMapEntries.add(builder.build());

  }

  /**
   * Recode each missing values with indexes like 8s and 9s.
   */
  protected void initializeMissingCategoryValueMapEntries() {
    if(missingValueMapEntries.isEmpty()) return;

    if(recodeCategoriesName) {
      int missIndex = 10 - missingValueMapEntries.size();
      int factor = 1;
      while(missIndex * factor < index + 1) {
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
    return new DerivedCategoricalVariableGenerator(originalTableReference, originalVariable, valueMapEntries, valueAt);
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

    public DerivedCategoricalVariableGenerator(String originalTableReference, VariableDto originalVariable, List<ValueMapEntry> valueMapEntries, int valueAt) {
      super(originalTableReference, originalVariable, valueMapEntries, valueAt);
    }

    public DerivedCategoricalVariableGenerator(VariableDto originalVariable, List<ValueMapEntry> valueMapEntries) {
      super(originalVariable, valueMapEntries);
    }

    @Override
    protected void generateScript() {
      scriptBuilder.append("$('").append(getOriginalVariableName()).append("')");
      if (getValueAt()>=0 && Strings.isNullOrEmpty(originalTableReference)) {
        scriptBuilder.append(".valueAt(").append(getValueAt()).append(")");
      }
      scriptBuilder.append(".map({");
      appendCategoryValueMapEntries();
      appendDistinctValueMapEntries();
      scriptBuilder.append("\n  }");
      appendSpecialValuesEntry(getOtherValuesMapEntry());
      appendSpecialValuesEntry(getEmptyValuesMapEntry());
      scriptBuilder.append(");");
    }
  }
}
