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

import com.google.gson.*;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The one JSON reading of the configuration model.
 * <p>
 * It is the configuration Gson that OrientDB was given, and it has two callers that must not drift apart: the
 * converters that store nested attributes ({@link JsonAttributeConverter}), and the upgrade step that reads the
 * documents OrientDB wrote. If they disagreed on a date format, the migration would read back dates the old store
 * never wrote.
 */
public final class DomainGson {

  private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

  private static final Gson GSON = new GsonBuilder() //
      .setDateFormat(DATE_PATTERN) //
      .registerTypeAdapter(Date.class, new DateDeserializer()) //
      .create();

  private DomainGson() {
  }

  public static Gson get() {
    return GSON;
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
