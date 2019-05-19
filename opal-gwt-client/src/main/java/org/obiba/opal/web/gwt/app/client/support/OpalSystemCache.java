/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.support;

import com.google.gwt.core.client.JsArrayString;
import org.obiba.opal.web.model.client.opal.GeneralConf;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;

import java.util.List;

public interface OpalSystemCache {

  void clearGeneralConf();

  void requestGeneralConf(GeneralConfHandler handler);

  void requestLocales(LocalesHandler handler);

  void clearTaxonomies();

  void requestTaxonomies(TaxonomiesHandler handler);

  interface GeneralConfHandler {
    void onGeneralConf(GeneralConf conf);
  }

  interface LocalesHandler {
    void onLocales(JsArrayString locales);
  }

  interface TaxonomiesHandler {
    void onTaxonomies(List<TaxonomyDto> taxonomies);
  }

}
