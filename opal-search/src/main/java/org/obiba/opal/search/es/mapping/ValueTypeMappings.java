/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search.es.mapping;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.obiba.magma.ValueType;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.LineStringType;
import org.obiba.magma.type.LocaleType;
import org.obiba.magma.type.PointType;
import org.obiba.magma.type.PolygonType;
import org.obiba.magma.type.TextType;

import com.google.common.collect.ImmutableMap;

public class ValueTypeMappings {

  private final Map<? extends ValueType, ValueTypeMapping> mapping;

  public ValueTypeMappings() {
    mapping = ImmutableMap.<ValueType, ValueTypeMapping>builder() //
        .put(TextType.get(), forType("string")) //
        .put(IntegerType.get(), forType("long")) //
        .put(DecimalType.get(), forType("double")) //
        .put(BooleanType.get(), forType("boolean")) //
        .put(DateType.get(), forTypeWithFormat("date", "date")) //
        .put(DateTimeType.get(), forType("date")) //
        .put(LocaleType.get(), forType("string")) //
        .put(BinaryType.get(), forType("binary")) //
        .put(PointType.get(), forType("string")) // point, multipoint
        .put(LineStringType.get(), forType("string")) // linestring
        .put(PolygonType.get(), forType("string")) // polygon, multipolygon
        .build();
  }

  @NotNull
  public ValueTypeMapping forType(ValueType type) {
    return mapping.get(type);
  }

  @NotNull
  private static ValueTypeMapping forType(String esType) {
    return new SimpleValueTypeMapping(esType);
  }

  @NotNull
  private static ValueTypeMapping forTypeWithFormat(String esType, String format) {
    return new SimpleValueTypeMapping(esType, format);
  }

  private static class SimpleValueTypeMapping implements ValueTypeMapping {

    @NotNull
    private final String esType;

    @Nullable
    private final String format;

    private SimpleValueTypeMapping(@NotNull String esType) {
      this(esType, null);
    }

    private SimpleValueTypeMapping(@NotNull String esType, @Nullable String format) {
      this.esType = esType;
      this.format = format;
    }

    @Override
    public XContentBuilder map(@NotNull XContentBuilder builder) {
      try {
        builder.field("type", esType);
        if(format != null) {
          builder.field("format", format);
        }
        return builder;
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }

  }
}
