/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.importvariables.support;

public enum DatasourceFileType {
  XLS,
  XLSX,
  XML,
  SAV,
  INVALID;

  public static DatasourceFileType getFileType(String filename) {
    DatasourceFileType type = INVALID;
    int position = !filename.isEmpty() ? filename.lastIndexOf('.') : -1;

    if(position > -1) {
      try {
        type = valueOf(filename.substring(position + 1).toUpperCase());
      } catch(Exception ignored) {
      }
    }

    return type;
  }

  //
  // Helper functions
  //

  public static boolean isExcelFile(String filename) {
    DatasourceFileType type = getFileType(filename);

    return type == XLS || type == XLSX;
  }

  public static boolean isXMLFile(String filename) {
    return XML == getFileType(filename);
  }
}
