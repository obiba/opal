package org.obiba.opal.datasource.util;

import java.util.List;

public class DatasourceUtil {

  public static String getDataPointValue(List<String> multipleDataValues) {
    if(multipleDataValues.size() != 0) {
      StringBuffer values = new StringBuffer();
      for(int i = 0; i < multipleDataValues.size(); i++) {
        values.append(multipleDataValues.get(i));
        if(i < multipleDataValues.size() - 1) {
          values.append(", ");
        }
      }

      return values.toString();
    }

    return null;
  }
}
