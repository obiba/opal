/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.domain;

import com.google.common.base.Strings;

public enum VCFSampleRole {
  SAMPLE,
  CONTROL;

  public static boolean isSample(String value) {
    return !Strings.isNullOrEmpty(value) && SAMPLE == valueOf(value.toUpperCase());
  }

  public static boolean isControl(String value) {
    return !Strings.isNullOrEmpty(value) && CONTROL == valueOf(value.toUpperCase());
  }
}
