/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.domain.converter;

import com.google.common.base.Strings;
import com.google.gson.*;
import jakarta.persistence.AttributeConverter;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Stores a structured attribute - a nested bean, a collection, a map - as JSON in a text column.
 * <p>
 * The configuration model is not a relational one. Its collections are small, are always read and written whole, and
 * nothing queries their members: no part of Opal asks for the projects whose tag set contains a value, or for the pod
 * specifications carrying a given toleration. Mapping them as element collections would add a join table apiece,
 * turning every read into a join and every write into a delete and reinsert, and buy nothing. The fields that
 * <em>are</em> queried are plain indexed columns.
 * <p>
 * The JSON is produced by the same Gson configuration that OrientDB was given, because that is what it already
 * stored: {@code copyToDocument} was {@code document.fromJSON(gson.toJson(obj))}. So this is the serialisation the
 * configuration has always had, in a column instead of a document.
 * <p>
 * The column is plain text, which both H2 and PostgreSQL have. Moving to a native JSON type later is a schema change,
 * not a code change.
 */
public abstract class JsonAttributeConverter<T> implements AttributeConverter<T, String> {

  private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

  protected static final Gson GSON = new GsonBuilder() //
      .setDateFormat(DATE_PATTERN) //
      .registerTypeAdapter(Date.class, new DateDeserializer()) //
      .create();

  /**
   * The attribute's full generic type, which erasure would otherwise lose.
   */
  protected abstract Type getType();

  @Override
  public String convertToDatabaseColumn(T attribute) {
    return attribute == null ? null : GSON.toJson(attribute);
  }

  @Override
  public T convertToEntityAttribute(String dbData) {
    return Strings.isNullOrEmpty(dbData) ? null : GSON.fromJson(dbData, getType());
  }

  /**
   * Reads the pattern Opal writes, and falls back on a timestamp for the values written before it was fixed.
   */
  private static class DateDeserializer implements JsonDeserializer<Date> {

    @Override
    public Date deserialize(JsonElement jsonElement, Type typeOf, JsonDeserializationContext context)
        throws JsonParseException {
      try {
        return new SimpleDateFormat(DATE_PATTERN).parse(jsonElement.getAsString());
      } catch(ParseException ignored) {
        // not the expected pattern, try a timestamp
      }
      try {
        return new Date(jsonElement.getAsLong());
      } catch(NumberFormatException e) {
        throw new JsonParseException("Unparseable date: " + jsonElement.getAsString());
      }
    }
  }
}
