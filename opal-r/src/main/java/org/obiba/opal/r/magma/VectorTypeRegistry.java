/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.r.magma;

import org.obiba.magma.ValueType;
import org.obiba.magma.type.*;

/**
 * A utility class for mapping {@code ValueType} to R {@code REXP}.
 */
public class VectorTypeRegistry {

  private VectorTypeRegistry() {}

  public static VectorType forValueType(ValueType type) {
    if (BinaryType.get().equals(type)) return new BinaryVectorType();
    if (BooleanType.get().equals(type)) return new BooleanVectorType();
    if (DateTimeType.get().equals(type)) return new DateTimeVectorType();
    if (DateType.get().equals(type)) return new DateVectorType();
    if (DecimalType.get().equals(type)) return new DecimalVectorType();
    if (IntegerType.get().equals(type)) return new IntegerVectorType();
    return new VectorType(type);
  }

}
