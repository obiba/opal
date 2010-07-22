/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma.support;

public class OpalResourceHelper {

  /**
   * Extract the datasource name from the keys table provided by configuration.
   * @param keysTableReference
   * @return
   */
  public static String extractKeysDatasourceName(String keysTableReference) {
    if(keysTableReference == null) {
      throw new IllegalArgumentException("null keysTableReference");
    }

    int separatorIndex = keysTableReference.indexOf('.');
    if(separatorIndex == -1) {
      throw new IllegalArgumentException("keysTableReference missing datasource");
    }

    return keysTableReference.substring(0, separatorIndex);
  }

}
