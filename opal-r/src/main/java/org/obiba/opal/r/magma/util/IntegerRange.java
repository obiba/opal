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

public class IntegerRange extends NumberRange {

  public IntegerRange(List<String> cats, List<String> missingCats) {
    super(cats, missingCats);
  }

  public int getMin() {
    return naRange.getFirst().intValue();
  }

  public int getMax() {
    return naRange.getLast().intValue();
  }

  @Override
  protected Number makeNumber(String valStr) {
    try {
      return Integer.parseInt(valStr);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}