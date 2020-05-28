/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.search.variables;

import com.gwtplatform.mvp.client.UiHandlers;
import org.obiba.opal.web.model.client.search.FacetResultDto;
import org.obiba.opal.web.model.client.search.ItemResultDto;

import java.util.List;

public interface SearchVariablesUiHandlers extends UiHandlers {

  void onSearchRange(String query, String rqlQuery, int offset, String sort);

  void onSearchAll(String query, SearchVariablesPresenter.QueryResultHandler handler);

  void onClear();

  void onFacet(String field, int size, FacetHandler handler);

  void onAddToCart(List<ItemResultDto> selectedItems);

  interface FacetHandler {

    void onResult(FacetResultDto facet);

  }

}
