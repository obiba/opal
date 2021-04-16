/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.magma.util;

import java.util.List;

public class DoubleRange extends NumberRange {

  public DoubleRange(List<String> cats, List<String> missingCats) {
    super(cats, missingCats);
  }

  public double getMin() {
    return naRange.get(0).doubleValue();
  }

  public double getMax() {
    return naRange.get(naRange.size() - 1).doubleValue();
  }

  @Override
  protected Number makeNumber(String valStr) {
    try {
      return Double.parseDouble(valStr);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
