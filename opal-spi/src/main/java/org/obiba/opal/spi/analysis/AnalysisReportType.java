/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.analysis;

public enum AnalysisReportType {
  HTML("html_document"),
  PDF("pdf_document");

  private final String output;

  AnalysisReportType(String output) {
    this.output = output;
  }

  public String getOutput() {
    return output;
  }

  /**
   * @param type
   * @return Returns proper type or HTML as default
   */
  public static AnalysisReportType safeValueOf(String type) {
    try {
      return valueOf(AnalysisReportType.class, type.replaceAll("'", "").toUpperCase());
    } catch (IllegalArgumentException | NullPointerException ignored) {
      return HTML;
    }
  }
}
