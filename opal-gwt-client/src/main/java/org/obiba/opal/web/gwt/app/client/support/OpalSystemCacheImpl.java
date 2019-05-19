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

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.Response;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.GeneralConf;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;

import java.util.List;

public class OpalSystemCacheImpl implements OpalSystemCache {

  private GeneralConf generalConf;

  private List<TaxonomyDto> taxonomies;

  @Override
  public void clearGeneralConf() {
    generalConf = null;
  }

  @Override
  public void requestGeneralConf(final GeneralConfHandler handler) {
    if (generalConf != null) handler.onGeneralConf(generalConf);
    else
      ResourceRequestBuilderFactory.<GeneralConf>newBuilder()
          .forResource(UriBuilders.SYSTEM_CONF_GENERAL.create().build())
          .withCallback(new ResourceCallback<GeneralConf>() {
            @Override
            public void onResource(Response response, GeneralConf resource) {
              generalConf = resource;
              handler.onGeneralConf(resource);
            }
          }).get().send();
  }

  @Override
  public void requestLocales(final LocalesHandler handler) {
    requestGeneralConf(new GeneralConfHandler() {
      @Override
      public void onGeneralConf(GeneralConf conf) {
        JsArrayString locales = JsArrayString.createArray().cast();
        for(int i = 0; i < conf.getLanguagesArray().length(); i++) {
          locales.push(conf.getLanguages(i));
        }
        handler.onLocales(locales);
      }
    });
  }

  @Override
  public void clearTaxonomies() {
    taxonomies = null;
  }

  @Override
  public void requestTaxonomies(final TaxonomiesHandler handler) {
    if (taxonomies != null && !taxonomies.isEmpty())
      handler.onTaxonomies(taxonomies);
    else
      ResourceRequestBuilderFactory.<JsArray<TaxonomyDto>>newBuilder()
          .forResource(UriBuilders.SYSTEM_CONF_TAXONOMIES.create().build()).get()
          .withCallback(new ResourceCallback<JsArray<TaxonomyDto>>() {
            @Override
            public void onResource(Response response, JsArray<TaxonomyDto> resource) {
              taxonomies = JsArrays.toList(resource);
              handler.onTaxonomies(taxonomies);
            }
          }).send();
  }
}
