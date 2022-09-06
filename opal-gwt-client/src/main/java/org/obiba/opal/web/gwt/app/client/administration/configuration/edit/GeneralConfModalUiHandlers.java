/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.configuration.edit;

import com.google.gwt.core.client.JsArrayString;
import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;

public interface GeneralConfModalUiHandlers extends ModalUiHandlers {

  void save(String name, String defaultCharSet, JsArrayString languages, String publicUrl, String logoutUrl);
}
