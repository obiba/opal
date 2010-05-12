package org.obiba.opal.web.client.smartgwt.client.smartgwt.data.fields;

import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.types.FieldType;

public class DataSourceFieldBuilder {

  private DataSourceField field;

  public DataSourceFieldBuilder(String name, FieldType type) {
    this.field = new DataSourceField(name, type);
  }

  public static DataSourceFieldBuilder newField(String name, FieldType type) {
    return new DataSourceFieldBuilder(name, type);
  }

  public DataSourceFieldBuilder type(FieldType type) {
    field.setType(type);
    return this;
  }

  public DataSourceFieldBuilder valueXPath(String valueXPath) {
    field.setValueXPath(valueXPath);
    return this;
  }

  public DataSourceFieldBuilder title(String title) {
    field.setTitle(title);
    return this;
  }

  public DataSourceFieldBuilder children() {
    field.setChildrenProperty(true);
    return this;
  }

  public DataSourceField build() {
    return field;
  }

}
