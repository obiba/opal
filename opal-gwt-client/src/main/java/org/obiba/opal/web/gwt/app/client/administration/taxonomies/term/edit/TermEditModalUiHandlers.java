/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.taxonomies.term.edit;

import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;
import org.obiba.opal.web.model.client.opal.LocaleTextDto;

import com.google.gwt.core.client.JsArray;
import com.gwtplatform.mvp.client.UiHandlers;

public interface TermEditModalUiHandlers extends UiHandlers, ModalUiHandlers {

  void onSave(String name, JsArray<LocaleTextDto> titles, JsArray<LocaleTextDto> descriptions);

}
