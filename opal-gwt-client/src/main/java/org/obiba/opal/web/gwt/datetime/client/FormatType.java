/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.datetime.client;

/**
 * Created with IntelliJ IDEA.
 * User: ymarcon
 * Date: 24/09/13
 * Time: 10:57 AM
 * To change this template use File | Settings | File Templates.
 */
public enum FormatType {

  TIME("LT"), //
  MONTH_NUMERAL("L"), //
  MONTH_NUMERAL_SHORT("l"), //
  MONTH_NAME("LL"), //
  MONTH_NAME_SHORT("ll"), //
  MONTH_NAME_TIME("LLL"), //
  MONTH_NAME_TIME_SHORT("lll"), //
  MONTH_NAME_DAY_TIME("LLLL"), //
  MONTH_NAME_DAY_TIME_SHORT("llll");

  private String code;

  private FormatType(String code) {
    this.code = code;
  }

  public String get() {
    return code;
  }

}
