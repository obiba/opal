/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.cart.edit;

import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;

import java.util.Map;

public interface CartVariableAttributeModalUiHandlers extends ModalUiHandlers {

  void onSubmit(String taxonomy, String vocabulary, String term);

  void onSubmit(String taxonomy, String vocabulary, Map<String, String> localizedValues);

}
