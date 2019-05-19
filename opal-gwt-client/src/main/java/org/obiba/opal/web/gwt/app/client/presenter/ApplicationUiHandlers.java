/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.presenter;

import org.obiba.opal.web.gwt.app.client.ui.VariableSuggestOracle;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.gwtplatform.mvp.client.UiHandlers;

public interface ApplicationUiHandlers extends UiHandlers {

  void onQuit();

  void onSelection(VariableSuggestOracle.VariableSuggestion suggestion);

  void onSearch(VariableSuggestOracle.AdvancedSearchSuggestion suggestion);

}
