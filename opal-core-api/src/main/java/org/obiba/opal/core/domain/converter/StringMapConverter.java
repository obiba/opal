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

import com.google.gson.reflect.TypeToken;

import jakarta.persistence.Converter;

import java.lang.reflect.Type;
import java.util.Map;

@Converter
public class StringMapConverter extends JsonAttributeConverter<Map<String, String>> {

  private static final Type TYPE = new TypeToken<Map<String, String>>() {
  }.getType();

  @Override
  protected Type getType() {
    return TYPE;
  }
}
