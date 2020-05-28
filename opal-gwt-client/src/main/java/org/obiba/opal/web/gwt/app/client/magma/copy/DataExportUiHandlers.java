/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma.copy;

import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;

public interface DataExportUiHandlers extends ModalUiHandlers {
  void cancel();

  /**
   *
   * @param dataFormat
   * @param out Destination folder or database depending on the data format.
   * @param idMapping
   */
  void onSubmit(String dataFormat, String out, String idMapping);
}
