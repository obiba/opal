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
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;

public class TemporalVariableDerivationHelper extends DerivationHelper {

  private static final DateTimeFormat MONTH_FORMAT = DateTimeFormat.getFormat("MM");

  private static final DateTimeFormat YEAR_FORMAT = DateTimeFormat.getFormat("yyyy");

  private final GroupMethod groupMethod;

  private final Date fromDate;

  private final Date toDate;

  public TemporalVariableDerivationHelper(VariableDto originalVariable, VariableDto destination, String groupMethod,
      Date fromDate, Date toDate) {
    super(originalVariable, destination);
    this.groupMethod = GroupMethod.valueOf(groupMethod);
    this.fromDate = new Date(fromDate.getTime());
    this.toDate = new Date(toDate.getTime());
    initializeValueMapEntries();
  }

  public GroupMethod getGroupMethod() {
    return groupMethod;
  }

  public Date getFromDate() {
    return new Date(fromDate.getTime());
  }

  public Date getToDate() {
    return new Date(toDate.getTime());
  }

  public static int getYear(Date date) {
    return Integer.parseInt(YEAR_FORMAT.format(date));
  }

  public static int getMonth(Date date) {
    return Integer.parseInt(MONTH_FORMAT.format(date));
  }

  @Override
  protected void initializeValueMapEntries() {
    valueMapEntries = new ArrayList<ValueMapEntry>();
    if(groupMethod == null) return;

    groupMethod.initializeValueMapEntries(getValueMapEntries(), fromDate, toDate);

    valueMapEntries.add(ValueMapEntry.createEmpties(translations.emptyValuesLabel()).build());
    valueMapEntries.add(ValueMapEntry.createOthers(translations.otherValuesLabel()).build());
  }

  @Override
  protected DerivedVariableGenerator getDerivedVariableGenerator() {
    return new DerivedTemporalVariableGenerator(originalVariable, valueMapEntries, groupMethod, fromDate, toDate);
  }

  public enum GroupMethod {
    HOUR_OF_DAY {
      @Override
      public void initializeValueMapEntries(List<ValueMapEntry> valueMapEntries, Date fromDate, Date toDate) {
        String hour = " " + translateTime("Hour");
        for(int i = 0; i < 24; i++) {
          String str = Integer.toString(i);
          valueMapEntries.add(ValueMapEntry.fromDistinct(str).label(i + "-" + (i + 1) + hour).newValue(str).build());
        }
      }

      @Override
      public String getScript(Date fromDate, Date toDate) {
        return "hourOfDay()";
      }

      @Override
      public boolean isTimeSpan() {
        return true;
      }

      @Override
      public boolean isForTimeType(String valueType) {
        return "datetime".equals(valueType);
      }
    },
    DAY_OF_WEEK {
      @Override
      public void initializeValueMapEntries(List<ValueMapEntry> valueMapEntries, Date fromDate, Date toDate) {
        valueMapEntries.add(ValueMapEntry.fromDistinct("1").label("Sunday").newValue("1").build());
        valueMapEntries.add(ValueMapEntry.fromDistinct("2").label("Monday").newValue("2").build());
        valueMapEntries.add(ValueMapEntry.fromDistinct("3").label("Tuesday").newValue("3").build());
        valueMapEntries.add(ValueMapEntry.fromDistinct("4").label("Wednesday").newValue("4").build());
        valueMapEntries.add(ValueMapEntry.fromDistinct("5").label("Thursday").newValue("5").build());
        valueMapEntries.add(ValueMapEntry.fromDistinct("6").label("Friday").newValue("6").build());
        valueMapEntries.add(ValueMapEntry.fromDistinct("7").label("Saturday").newValue("7").build());
      }

      @Override
      public String getScript(Date fromDate, Date toDate) {
        return "dayOfWeek()";
      }

      @Override
      public boolean isTimeSpan() {
        return true;
      }
    },
    DAY_OF_MONTH {
      @Override
      public void initializeValueMapEntries(List<ValueMapEntry> valueMapEntries, Date fromDate, Date toDate) {
        String day = translateTime("Day") + " ";
        for(int i = 1; i < 32; i++) {
          String str = Integer.toString(i);
          valueMapEntries.add(ValueMapEntry.fromDistinct(str).label(day + str).newValue(str).build());
        }
      }

      @Override
      public String getScript(Date fromDate, Date toDate) {
        return "dayOfMonth()";
      }

      @Override
      public boolean isTimeSpan() {
        return true;
      }
    },
    DAY_OF_YEAR {
      @Override
      public void initializeValueMapEntries(List<ValueMapEntry> valueMapEntries, Date fromDate, Date toDate) {
        String day = translateTime("Day") + " ";
        for(int i = 1; i < 366; i++) {
          String str = Integer.toString(i);
          valueMapEntries.add(ValueMapEntry.fromDistinct(str).label(day + str).newValue(str).build());
        }
      }

      @Override
      public String getScript(Date fromDate, Date toDate) {
        return "dayOfYear()";
      }

      @Override
      public boolean isTimeSpan() {
        return true;
      }
    },
    WEEK_OF_MONTH {
      @Override
      public void initializeValueMapEntries(List<ValueMapEntry> valueMapEntries, Date fromDate, Date toDate) {
        String week = translateTime("Week") + " ";
        for(int i = 1; i < 5; i++) {
          String str = Integer.toString(i);
          valueMapEntries.add(ValueMapEntry.fromDistinct(str).label(week + str).newValue(str).build());
        }
      }

      @Override
      public String getScript(Date fromDate, Date toDate) {
        return "weekOfMonth()";
      }

      @Override
      public boolean isTimeSpan() {
        return true;
      }
    },
    WEEK_OF_YEAR {
      @Override
      public void initializeValueMapEntries(List<ValueMapEntry> valueMapEntries, Date fromDate, Date toDate) {
        String week = translateTime("Week") + " ";
        for(int i = 1; i < 53; i++) {
          String str = Integer.toString(i);
          valueMapEntries.add(ValueMapEntry.fromDistinct(str).label(week + str).newValue(str).build());
        }
      }

      @Override
      public String getScript(Date fromDate, Date toDate) {
        return "weekOfYear()";
      }

      @Override
      public boolean isTimeSpan() {
        return true;
      }
    },
    MONTH_OF_YEAR {
      @Override
      public void initializeValueMapEntries(List<ValueMapEntry> valueMapEntries, Date fromDate, Date toDate) {
        for(int i = 1; i < 13; i++) {
          valueMapEntries.add(
              ValueMapEntry.fromDistinct(Integer.toString(i - 1)).label(translateMonth(i)).newValue(Integer.toString(i))
                  .build());
        }
      }

      @Override
      public String getScript(Date fromDate, Date toDate) {
        return "month()";
      }

      @Override
      public boolean isTimeSpan() {
        return true;
      }
    },
    QUARTER_OF_YEAR {
      @Override
      public void initializeValueMapEntries(List<ValueMapEntry> valueMapEntries, Date fromDate, Date toDate) {
        String quarter = translateTime("Quarter") + " ";
        for(int i = 1; i < 5; i++) {
          String str = Integer.toString(i);
          valueMapEntries
              .add(ValueMapEntry.fromDistinct(Integer.toString(i - 1)).label(quarter + str).newValue(str).build());
        }
      }

      @Override
      public String getScript(Date fromDate, Date toDate) {
        return "quarter()";
      }

      @Override
      public boolean isTimeSpan() {
        return true;
      }
    },
    SEMESTER_OF_YEAR {
      @Override
      public void initializeValueMapEntries(List<ValueMapEntry> valueMapEntries, Date fromDate, Date toDate) {
        String semester = translateTime("Semester") + " ";
        for(int i = 1; i < 3; i++) {
          String str = Integer.toString(i);
          valueMapEntries
              .add(ValueMapEntry.fromDistinct(Integer.toString(i - 1)).label(semester + str).newValue(str).build());
        }
      }

      @Override
      public String getScript(Date fromDate, Date toDate) {
        return "semester()";
      }

      @Override
      public boolean isTimeSpan() {
        return true;
      }
    }, //
    MONTH {
      @Override
      public void initializeValueMapEntries(List<ValueMapEntry> valueMapEntries, Date fromDate, Date toDate) {
        int idx = 1;
        int fromYear = getYear(fromDate);
        int toYear = getYear(toDate);
        int fromMonth = getMonth(fromDate);
        int toMonth = getMonth(toDate);
        for(long i = fromYear; i <= toYear; i++) {
          int firstMonth = i == fromYear ? fromMonth : 1;
          int lastMonth = i == toYear ? toMonth : 12;
          for(int j = firstMonth; j <= lastMonth; j++) {
            String value = i + "-" + (j < 10 ? "0" : "") + j;
            valueMapEntries.add(
                ValueMapEntry.fromDistinct(value).label(translateMonth(j) + ", " + i).newValue(Integer.toString(idx++))
                    .build());
          }
        }
      }

      @Override
      public String getScript(Date fromDate, Date toDate) {
        return "format('yyyy-MM')";
      }

      @Override
      public boolean isTimeSpan() {
        return false;
      }
    },
    QUARTER {
      @Override
      public void initializeValueMapEntries(List<ValueMapEntry> valueMapEntries, Date fromDate, Date toDate) {
        int fromYear = getYear(fromDate);
        int toYear = getYear(toDate);
        int fromMonth = getMonth(fromDate);
        int toMonth = getMonth(toDate);
        String quarter = translateTime("Quarter") + " ";
        int idx = 1;
        for(long i = fromYear; i <= toYear; i++) {
          int firstQuarter = i == fromYear ? monthToQuarter(fromMonth) : 1;
          int lastQuarter = i == toYear ? monthToQuarter(toMonth) : 4;
          for(int j = firstQuarter; j <= lastQuarter; j++) {
            String value = i + "-" + j;
            valueMapEntries.add(
                ValueMapEntry.fromDistinct(value).label(quarter + j + ", " + i).newValue(Integer.toString(idx++))
                    .build());
          }
        }
      }

      private int monthToQuarter(int month) {
        if(month <= 3) {
          return 1;
        }
        if(month <= 6) {
          return 2;
        }
        if(month <= 9) {
          return 3;
        }
        return 4;
      }

      @Override
      public String getScript(Date fromDate, Date toDate) {
        StringBuilder builder = new StringBuilder("format('yyyy-MM').map({");
        int fromYear = getYear(fromDate);
        int toYear = getYear(toDate);
        int fromMonth = getMonth(fromDate);
        int toMonth = getMonth(toDate);
        for(long i = fromYear; i <= toYear; i++) {
          int firstMonth = i == fromYear ? fromMonth : 1;
          int lastMonth = i == toYear ? toMonth : 12;
          for(int j = firstMonth; j <= lastMonth; j++) {
            String value = i + "-" + (j < 10 ? "0" : "") + j;
            builder.append("'").append(value).append("':'").append(i).append("-").append(monthToQuarter(j)).append("'");
            if(j < lastMonth || i < toYear) {
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
      public void initializeValueMapEntries(List<ValueMapEntry> valueMapEntries, Date fromDate, Date toDate) {
        String quarter = translateTime("Semester") + " ";
        int idx = 1;
        int fromYear = getYear(fromDate);
        int toYear = getYear(toDate);
        int fromMonth = getMonth(fromDate);
        int toMonth = getMonth(toDate);
        for(long i = fromYear; i <= toYear; i++) {
          int firstSemester = i == fromYear ? monthToSemester(fromMonth) : 1;
          int lastSemester = i == toYear ? monthToSemester(toMonth) : 4;
          for(int j = firstSemester; j <= lastSemester; j++) {
            String value = i + "-" + j;
            valueMapEntries.add(
                ValueMapEntry.fromDistinct(value).label(quarter + j + ", " + i).newValue(Integer.toString(idx++))
                    .build());
          }
        }
      }

      private int monthToSemester(int month) {
        return month <= 6 ? 1 : 2;
      }

      @Override
      public String getScript(Date fromDate, Date toDate) {
        StringBuilder builder = new StringBuilder("format('yyyy-MM').map({");
        int fromYear = getYear(fromDate);
        int toYear = getYear(toDate);
        int fromMonth = getMonth(fromDate);
        int toMonth = getMonth(toDate);
        for(long i = fromYear; i <= toYear; i++) {
          int firstMonth = i == fromYear ? fromMonth : 1;
          int lastMonth = i == toYear ? toMonth : 12;
          for(int j = firstMonth; j <= lastMonth; j++) {
            String value = i + "-" + (j < 10 ? "0" : "") + j;
            builder.append("'").append(value).append("':'").append(i).append("-").append(monthToSemester(j))
                .append("'");
            if(j < lastMonth || i < toYear) {
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
      public void initializeValueMapEntries(List<ValueMapEntry> valueMapEntries, Date fromDate, Date toDate) {
        String year = translateTime("Year") + " ";
        int idx = 1;
        int fromYear = getYear(fromDate);
        int toYear = getYear(toDate);
        for(long i = fromYear; i <= toYear; i++) {
          String str = Long.toString(i);
          valueMapEntries
              .add(ValueMapEntry.fromDistinct(str).label(year + str).newValue(Integer.toString(idx++)).build());
        }
      }

      @Override
      public String getScript(Date fromDate, Date toDate) {
        return "year()";
      }

      @Override
      public boolean isTimeSpan() {
        return false;
      }
    },
    LUSTRUM {
      @Override
      public void initializeValueMapEntries(List<ValueMapEntry> valueMapEntries, Date fromDate, Date toDate) {
        int fromYear = getYear(fromDate);
        int toYear = getYear(toDate);
        String lustrum = translateTime("Lustrum") + " ";
        int idx = 1;
        long i = fromYear;
        while(i < toYear) {
          String str = Long.toString(i) + "-" + Long.toString(i + 5);
          valueMapEntries
              .add(ValueMapEntry.fromDistinct(str).label(lustrum + str).newValue(Integer.toString(idx++)).build());
          i = i + 5;
        }
      }

      @Override
      public String getScript(Date fromDate, Date toDate) {
        StringBuilder builder = new StringBuilder("year().map({");
        int fromYear = getYear(fromDate);
        int toYear = getYear(toDate);
        for(long i = fromYear; i < toYear; i++) {
          for(int j = 0; j < 5; j++) {
            builder.append("'").append(i + j).append("':'");
            builder.append(i).append("-").append(i + 5).append("'");
            if(j < 4 || i < toYear - 1) {
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
      public void initializeValueMapEntries(List<ValueMapEntry> valueMapEntries, Date fromDate, Date toDate) {
        int fromYear = getYear(fromDate);
        int toYear = getYear(toDate);
        String year = translateTime("Decade") + " ";
        int idx = 1;
        long i = fromYear;
        while(i < toYear) {
          String str = Long.toString(i) + "-" + Long.toString(i + 10);
          valueMapEntries
              .add(ValueMapEntry.fromDistinct(str).label(year + str).newValue(Integer.toString(idx++)).build());
          i = i + 10;
        }
      }

      @Override
      public String getScript(Date fromDate, Date toDate) {
        StringBuilder builder = new StringBuilder("year().map({");
        int fromYear = getYear(fromDate);
        int toYear = getYear(toDate);
        for(long i = fromYear; i < toYear; i++) {
          for(int j = 0; j < 10; j++) {
            builder.append("'").append(i + j).append("':'");
            builder.append(i).append("-").append(i + 10).append("'");
            if(j < 9 || i < toYear - 1) {
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

//    protected Translations translations = GWT.create(Translations.class);

    public abstract boolean isTimeSpan();

    public abstract String getScript(Date fromDate, Date toDate);

    public abstract void initializeValueMapEntries(List<ValueMapEntry> valueMapEntries, Date fromDate, Date toDate);

    public boolean isForTimeType(String valueType) {
      return "date".equals(valueType) || "datetime".equals(valueType);
    }

    protected String translateTime(String text) {
      Translations translations = GWT.create(Translations.class);
      return translations.timeMap().get(text);
    }

    @SuppressWarnings("PMD.NcssMethodCount")
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

  }

}