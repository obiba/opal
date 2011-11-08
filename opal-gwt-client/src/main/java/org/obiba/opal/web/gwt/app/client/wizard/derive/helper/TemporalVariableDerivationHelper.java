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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry.ValueMapEntryType;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

public class TemporalVariableDerivationHelper extends DerivationHelper {

  private final GroupMethod groupMethod;

  private final Date fromDate;

  private final Date toDate;

  public TemporalVariableDerivationHelper(VariableDto originalVariable, String groupMethod, Date fromDate, Date toDate) {
    super(originalVariable);
    this.groupMethod = GroupMethod.valueOf(groupMethod);
    this.fromDate = fromDate;
    this.toDate = toDate;
    initializeValueMapEntries();
  }

  public GroupMethod getGroupMethod() {
    return groupMethod;
  }

  public Date getFromDate() {
    return fromDate;
  }

  public int getFromYear() {
    return Integer.parseInt(DateTimeFormat.getFormat(PredefinedFormat.YEAR).format(fromDate));
  }

  public Date getToDate() {
    return toDate;
  }

  public int getToYear() {
    return Integer.parseInt(DateTimeFormat.getFormat(PredefinedFormat.YEAR).format(toDate));
  }

  @Override
  protected void initializeValueMapEntries() {
    this.valueMapEntries = new ArrayList<ValueMapEntry>();
    if(groupMethod == null) return;

    groupMethod.initializeValueMapEntries(this);

    valueMapEntries.add(ValueMapEntry.createEmpties(translations.emptyValuesLabel()).build());
    valueMapEntries.add(ValueMapEntry.createOthers(translations.otherValuesLabel()).build());
  }

  @Override
  public VariableDto getDerivedVariable() {
    VariableDto derived = copyVariable(originalVariable, true);
    derived.setValueType("text");

    Map<String, CategoryDto> newCategoriesMap = new LinkedHashMap<String, CategoryDto>();

    StringBuilder scriptBuilder = new StringBuilder("$('" + originalVariable.getName() + "')");
    scriptBuilder.append(".").append(groupMethod.getScript(this));
    scriptBuilder.append(".map({");

    boolean first = true;
    for(ValueMapEntry entry : valueMapEntries) {
      if(entry.getType().equals(ValueMapEntryType.DISTINCT_VALUE)) {
        if(first) {
          first = false;
        } else {
          scriptBuilder.append(",");
        }
        scriptBuilder.append("\n    '").append(entry.getValue()).append("': ");
        appendNewValue(scriptBuilder, entry);

        CategoryDto cat = newCategory(entry);
        cat.setAttributesArray(newAttributes(newLabelAttribute(entry)));
        newCategoriesMap.put(entry.getNewValue(), cat);
      }
    }

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

  public enum GroupMethod {
    HOUR_OF_DAY {
      @Override
      public void initializeValueMapEntries(TemporalVariableDerivationHelper helper) {
        String hour = translateTime("Hour") + " ";
        for(int i = 0; i < 24; i++) {
          String str = Integer.toString(i);
          addValueMapEntry(helper, ValueMapEntry.fromDistinct(str).label(hour + (i + 1)).newValue(str).build());
        }
      }

      @Override
      public String getScript(TemporalVariableDerivationHelper helper) {
        return "hourOfDay()";
      }

      @Override
      public boolean isTimeSpan() {
        return true;
      }

      @Override
      public boolean isForTimeType(String valueType) {
        return valueType.equals("datetime");
      }
    },
    DAY_OF_WEEK {
      @Override
      public void initializeValueMapEntries(TemporalVariableDerivationHelper helper) {
        addValueMapEntry(helper, ValueMapEntry.fromDistinct("1").label("Sunday").newValue("1").build());
        addValueMapEntry(helper, ValueMapEntry.fromDistinct("2").label("Monday").newValue("2").build());
        addValueMapEntry(helper, ValueMapEntry.fromDistinct("3").label("Tuesday").newValue("3").build());
        addValueMapEntry(helper, ValueMapEntry.fromDistinct("4").label("Wednesday").newValue("4").build());
        addValueMapEntry(helper, ValueMapEntry.fromDistinct("5").label("Thursday").newValue("5").build());
        addValueMapEntry(helper, ValueMapEntry.fromDistinct("6").label("Friday").newValue("6").build());
        addValueMapEntry(helper, ValueMapEntry.fromDistinct("7").label("Saturday").newValue("7").build());
      }

      @Override
      public String getScript(TemporalVariableDerivationHelper helper) {
        return "dayOfWeek()";
      }

      @Override
      public boolean isTimeSpan() {
        return true;
      }
    },
    DAY_OF_MONTH {
      @Override
      public void initializeValueMapEntries(TemporalVariableDerivationHelper helper) {
        String day = translateTime("Day") + " ";
        for(int i = 1; i < 32; i++) {
          String str = Integer.toString(i);
          addValueMapEntry(helper, ValueMapEntry.fromDistinct(str).label(day + str).newValue(str).build());
        }
      }

      @Override
      public String getScript(TemporalVariableDerivationHelper helper) {
        return "dayOfMonth()";
      }

      @Override
      public boolean isTimeSpan() {
        return true;
      }
    },
    DAY_OF_YEAR {
      @Override
      public void initializeValueMapEntries(TemporalVariableDerivationHelper helper) {
        String day = translateTime("Day") + " ";
        for(int i = 1; i < 366; i++) {
          String str = Integer.toString(i);
          addValueMapEntry(helper, ValueMapEntry.fromDistinct(str).label(day + str).newValue(str).build());
        }
      }

      @Override
      public String getScript(TemporalVariableDerivationHelper helper) {
        return "dayOfYear()";
      }

      @Override
      public boolean isTimeSpan() {
        return true;
      }
    },
    WEEK_OF_MONTH {
      @Override
      public void initializeValueMapEntries(TemporalVariableDerivationHelper helper) {
        String week = translateTime("Week") + " ";
        for(int i = 1; i < 5; i++) {
          String str = Integer.toString(i);
          addValueMapEntry(helper, ValueMapEntry.fromDistinct(str).label(week + str).newValue(str).build());
        }
      }

      @Override
      public String getScript(TemporalVariableDerivationHelper helper) {
        return "weekOfMonth()";
      }

      @Override
      public boolean isTimeSpan() {
        return true;
      }
    },
    WEEK_OF_YEAR {

      @Override
      public void initializeValueMapEntries(TemporalVariableDerivationHelper helper) {
        String week = translateTime("Week") + " ";
        for(int i = 1; i < 53; i++) {
          String str = Integer.toString(i);
          addValueMapEntry(helper, ValueMapEntry.fromDistinct(str).label(week + str).newValue(str).build());
        }
      }

      @Override
      public String getScript(TemporalVariableDerivationHelper helper) {
        return "weekOfYear()";
      }

      @Override
      public boolean isTimeSpan() {
        return true;
      }
    },
    MONTH_OF_YEAR {
      @Override
      public void initializeValueMapEntries(TemporalVariableDerivationHelper helper) {
        for(int i = 1; i < 13; i++) {
          String str = Integer.toString(i);
          addValueMapEntry(helper, ValueMapEntry.fromDistinct(str).label(translateMonth(i)).newValue(str).build());
        }
      }

      @Override
      public String getScript(TemporalVariableDerivationHelper helper) {
        return "month()";
      }

      @Override
      public boolean isTimeSpan() {
        return true;
      }
    },
    QUARTER_OF_YEAR {

      @Override
      public void initializeValueMapEntries(TemporalVariableDerivationHelper helper) {
        String quarter = translateTime("Quarter") + " ";
        for(int i = 1; i < 5; i++) {
          String str = Integer.toString(i);
          addValueMapEntry(helper, ValueMapEntry.fromDistinct(str).label(quarter + str).newValue(str).build());
        }
      }

      @Override
      public String getScript(TemporalVariableDerivationHelper helper) {
        // TODO quarterOfYear()
        return "month().map({1:1,2:1,3:1,4:2,5:2,6:2,7:3,8:3,9:3,10:4,11:4,12:4},null)";
      }

      @Override
      public boolean isTimeSpan() {
        return true;
      }
    },
    SEMESTER_OF_YEAR {
      @Override
      public void initializeValueMapEntries(TemporalVariableDerivationHelper helper) {
        String semester = translateTime("Semester") + " ";
        for(int i = 1; i < 3; i++) {
          String str = Integer.toString(i);
          addValueMapEntry(helper, ValueMapEntry.fromDistinct(str).label(semester + str).newValue(str).build());
        }
      }

      @Override
      public String getScript(TemporalVariableDerivationHelper helper) {
        // TODO semesterOfYear()
        return "month().map({1:1,2:1,3:1,4:1,5:1,6:1,7:2,8:2,9:2,10:2,11:2,12:2},null)";
      }

      @Override
      public boolean isTimeSpan() {
        return true;
      }
    }, //
    MONTH {
      @Override
      public void initializeValueMapEntries(TemporalVariableDerivationHelper helper) {
        int idx = 1;
        for(long i = helper.getFromYear(); i < helper.getToYear(); i++) {
          for(int j = 1; j < 13; j++) {
            String value = i + "-" + (j < 10 ? "0" : "") + j;
            addValueMapEntry(helper, ValueMapEntry.fromDistinct(value).label(translateMonth(j) + ", " + i).newValue(Integer.toString(idx++)).build());
          }
        }
      }

      @Override
      public String getScript(TemporalVariableDerivationHelper helper) {
        return "format('yyyy-MM')";
      }

      @Override
      public boolean isTimeSpan() {
        return false;
      }
    },
    QUARTER {

      @Override
      public void initializeValueMapEntries(TemporalVariableDerivationHelper helper) {
        String quarter = translateTime("Quarter") + " ";
        int idx = 1;
        for(long i = helper.getFromYear(); i < helper.getToYear(); i++) {
          for(int j = 1; j < 5; j++) {
            String value = i + "-" + j;
            addValueMapEntry(helper, ValueMapEntry.fromDistinct(value).label(quarter + j + ", " + i).newValue(Integer.toString(idx++)).build());
          }
        }
      }

      @Override
      public String getScript(TemporalVariableDerivationHelper helper) {
        StringBuilder builder = new StringBuilder("format('yyyy-MM').map({");
        for(long i = helper.getFromYear(); i < helper.getToYear(); i++) {
          for(int j = 1; j < 13; j++) {
            String value = i + "-" + (j < 10 ? "0" : "") + j;
            builder.append("'").append(value).append("':'");
            if(j <= 3) {
              builder.append(i).append("-1'");
            } else if(j <= 6) {
              builder.append(i).append("-2'");
            } else if(j <= 9) {
              builder.append(i).append("-3'");
            } else {
              builder.append(i).append("-4'");
            }
            if(j < 12 || i < helper.getToYear() - 1) {
              builder.append(", ");
            }
          }
        }
        builder.append("},null)");
        return builder.toString();
      }

      @Override
      public boolean isTimeSpan() {
        return false;
      }
    },
    SEMESTER {

      @Override
      public void initializeValueMapEntries(TemporalVariableDerivationHelper helper) {
        String quarter = translateTime("Semester") + " ";
        int idx = 1;
        for(long i = helper.getFromYear(); i < helper.getToYear(); i++) {
          for(int j = 1; j < 3; j++) {
            String value = i + "-" + j;
            addValueMapEntry(helper, ValueMapEntry.fromDistinct(value).label(quarter + j + ", " + i).newValue(Integer.toString(idx++)).build());
          }
        }
      }

      @Override
      public String getScript(TemporalVariableDerivationHelper helper) {
        StringBuilder builder = new StringBuilder("format('yyyy-MM').map({");
        for(long i = helper.getFromYear(); i < helper.getToYear(); i++) {
          for(int j = 1; j < 13; j++) {
            String value = i + "-" + (j < 10 ? "0" : "") + j;
            builder.append("'").append(value).append("':'");
            if(j <= 6) {
              builder.append(i).append("-1'");
            } else {
              builder.append(i).append("-2'");
            }
            if(j < 12 || i < helper.getToYear() - 1) {
              builder.append(", ");
            }
          }
        }
        builder.append("},null)");
        return builder.toString();
      }

      @Override
      public boolean isTimeSpan() {
        return false;
      }

    },
    YEAR {
      @Override
      public void initializeValueMapEntries(TemporalVariableDerivationHelper helper) {
        String year = translateTime("Year") + " ";
        int idx = 1;
        for(long i = helper.getFromYear(); i < helper.getToYear(); i++) {
          String str = Long.toString(i);
          addValueMapEntry(helper, ValueMapEntry.fromDistinct(str).label(year + str).newValue(Integer.toString(idx++)).build());
        }
      }

      @Override
      public String getScript(TemporalVariableDerivationHelper helper) {
        return "year()";
      }

      @Override
      public boolean isTimeSpan() {
        return false;
      }
    },
    LUSTRUM {
      @Override
      public void initializeValueMapEntries(TemporalVariableDerivationHelper helper) {
        String lustrum = translateTime("Lustrum") + " ";
        int idx = 1;
        long i = helper.getFromYear();
        while(i < helper.getToYear()) {
          String str = Long.toString(i) + "-" + Long.toString(i + 5);
          addValueMapEntry(helper, ValueMapEntry.fromDistinct(str).label(lustrum + str).newValue(Integer.toString(idx++)).build());
          i = i + 5;
        }
      }

      @Override
      public String getScript(TemporalVariableDerivationHelper helper) {
        StringBuilder builder = new StringBuilder("year().map({");
        for(long i = helper.getFromYear(); i < helper.getToYear(); i++) {
          for(int j = 0; j < 5; j++) {
            builder.append("'").append(i + j).append("':'");
            builder.append(i).append("-").append(i + 5).append("'");
            if(j < 4 || i < helper.getToYear() - 1) {
              builder.append(", ");
            }
          }
          i = i + 4;
        }
        builder.append("})");
        return builder.toString();
      }

      @Override
      public boolean isTimeSpan() {
        return false;
      }
    },
    DECADE {
      @Override
      public void initializeValueMapEntries(TemporalVariableDerivationHelper helper) {
        String year = translateTime("Decade") + " ";
        int idx = 1;
        long i = helper.getFromYear();
        while(i < helper.getToYear()) {
          String str = Long.toString(i) + "-" + Long.toString(i + 10);
          addValueMapEntry(helper, ValueMapEntry.fromDistinct(str).label(year + str).newValue(Integer.toString(idx++)).build());
          i = i + 10;
        }
      }

      @Override
      public String getScript(TemporalVariableDerivationHelper helper) {
        StringBuilder builder = new StringBuilder("year().map({");
        for(long i = helper.getFromYear(); i < helper.getToYear(); i++) {
          for(int j = 0; j < 10; j++) {
            builder.append("'").append(i + j).append("':'");
            builder.append(i).append("-").append(i + 10).append("'");
            if(j < 9 || i < helper.getToYear() - 1) {
              builder.append(", ");
            }
          }
          i = i + 9;
        }
        builder.append("})");
        return builder.toString();
      }

      @Override
      public boolean isTimeSpan() {
        return false;
      }
    };

    protected Translations translations = GWT.create(Translations.class);

    public abstract boolean isTimeSpan();

    public abstract String getScript(TemporalVariableDerivationHelper helper);

    public abstract void initializeValueMapEntries(TemporalVariableDerivationHelper helper);

    public boolean isForTimeType(String valueType) {
      return valueType.equals("date") || valueType.equals("datetime");
    }

    protected String translateTime(String text) {
      return translations.timeMap().get(text);
    }

    protected String translateMonth(int i) {
      switch(i) {
      case 1:
        return translateTime("January");
      case 2:
        return translateTime("February");
      case 3:
        return translateTime("March");
      case 4:
        return translateTime("April");
      case 5:
        return translateTime("May");
      case 6:
        return translateTime("June");
      case 7:
        return translateTime("July");
      case 8:
        return translateTime("August");
      case 9:
        return translateTime("September");
      case 10:
        return translateTime("October");
      case 11:
        return translateTime("November");
      case 12:
        return translateTime("December");
      default:
        return "";
      }
    }

    protected void addValueMapEntry(TemporalVariableDerivationHelper helper, ValueMapEntry entry) {
      helper.getValueMapEntries().add(entry);
    }
  }

}