/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.datashield.cfg;

import com.google.gson.reflect.TypeToken;

import jakarta.persistence.Converter;

import org.obiba.datashield.core.DSMethodType;
import org.obiba.datashield.core.impl.DefaultDSMethod;
import org.obiba.opal.core.domain.converter.JsonAttributeConverter;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

@Converter
public class DSEnvironmentsConverter extends JsonAttributeConverter<Map<DSMethodType, List<DefaultDSMethod>>> {

  private static final Type TYPE = new TypeToken<Map<DSMethodType, List<DefaultDSMethod>>>() {
  }.getType();

  @Override
  protected Type getType() {
    return TYPE;
  }
}
