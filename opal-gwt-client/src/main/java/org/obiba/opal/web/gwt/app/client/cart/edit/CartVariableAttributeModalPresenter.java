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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import org.obiba.opal.web.gwt.app.client.cart.service.CartVariableItem;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.support.MagmaPath;
import org.obiba.opal.web.gwt.app.client.support.OpalSystemCache;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class CartVariableAttributeModalPresenter
    extends ModalPresenterWidget<CartVariableAttributeModalPresenter.Display>
    implements CartVariableAttributeModalUiHandlers {

  private Map<String, List<CartVariableItem>> cartVariableItemsMap = Maps.newHashMap();

  private final OpalSystemCache opalSystemCache;

  private boolean apply;

  private int progressCount = 0;

  private int errorCount = 0;

  @Inject
  public CartVariableAttributeModalPresenter(EventBus eventBus, Display display, OpalSystemCache opalSystemCache) {
    super(eventBus, display);
    this.opalSystemCache = opalSystemCache;
    getView().setUiHandlers(this);
  }

  public void initialize(List<CartVariableItem> cartVariableItems, boolean apply) {
    this.apply = apply;
    getView().setMode(apply);
    renderTaxonomies();
    renderLocales();
    cartVariableItemsMap.clear();
    for (CartVariableItem item : cartVariableItems) {
      String tableRef = item.getTableReference();
      if (!cartVariableItemsMap.containsKey(tableRef))
        cartVariableItemsMap.put(tableRef, new ArrayList<CartVariableItem>());
      cartVariableItemsMap.get(tableRef).add(item);
    }
  }

  private void renderTaxonomies() {
    opalSystemCache.requestTaxonomies(new OpalSystemCache.TaxonomiesHandler() {
      @Override
      public void onTaxonomies(List<TaxonomyDto> taxonomies) {
        getView().setTaxonomies(taxonomies);
      }
    });
  }

  private void renderLocales() {
    opalSystemCache.requestLocales(new OpalSystemCache.LocalesHandler() {
      @Override
      public void onLocales(JsArrayString locales) {
        getView().setLocales(locales);
      }
    });
  }

  @Override
  public void onSubmit(String taxonomy, String vocabulary, String term) {
    getView().setBusy(true);
    progressCount = 0;
    for (String tableRef : cartVariableItemsMap.keySet()) {
      onSubmit(tableRef, taxonomy, vocabulary, term);
    }
  }

  @Override
  public void onSubmit(String taxonomy, String vocabulary, Map<String, String> localizedValues) {
    getView().setBusy(true);
    progressCount = 0;
    for (String tableRef : cartVariableItemsMap.keySet()) {
      if (apply) {
        List<String> locales = Lists.newArrayList(localizedValues.keySet());
        List<String> values = Lists.newArrayList(localizedValues.values());
        onSubmit(tableRef, taxonomy, vocabulary, locales, values);
      } else
        onSubmit(tableRef, taxonomy, vocabulary, null);
    }
  }
  private void onSubmit(String tableRef, String namespace, String name, String value) {
    onSubmit(tableRef, namespace, name, null, Strings.isNullOrEmpty(value) ? null : Lists.newArrayList(value));
  }
  private void onSubmit(String tableRef, String namespace, String name, List<String> locales, List<String> values) {
    final MagmaPath.Parser parser = MagmaPath.Parser.parse(tableRef);
    UriBuilder uriBuilder = UriBuilders.DATASOURCE_TABLE_VARIABLES_ATTRIBUTE.create()
        .query("namespace", namespace)
        .query("name", name);
    if (locales != null) {
      for (String locale : locales)
        uriBuilder.query("locale", locale);
    }
    if (values != null){
      for (String value : values)
        uriBuilder.query("value", value);
    }
    if (!apply) uriBuilder.query("action", "delete");
    ResourceRequestBuilder builder = ResourceRequestBuilderFactory.newBuilder()
        .forResource(uriBuilder.build(parser.getDatasource(), parser.getTable()))
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            incrementProgress(parser, response.getStatusCode());
          }
        }, Response.SC_BAD_REQUEST, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_FORBIDDEN, Response.SC_NOT_FOUND, Response.SC_OK);
    for (CartVariableItem item : cartVariableItemsMap.get(tableRef))
      builder.withFormBody("variable", item.getVariable().getName());
    builder.put().send();
  }

  private void incrementProgress(MagmaPath.Parser parser, int status) {
    int count = cartVariableItemsMap.get(parser.getTableReference()).size();
    progressCount++;
    int percent = (progressCount * 100) / cartVariableItemsMap.keySet().size();

    if (status == Response.SC_OK) {
      String msgKey = apply ? "VariablesAnnotationApplied" : "VariablesAnnotationRemoved";
      getView().setProgress(msgKey, parser.getTableReference(), count, percent);
    } else {
      errorCount++;
      fireEvent(NotificationEvent.newBuilder().error("VariablesAnnotationFailure").args(parser.getTableReference()).build());
      getView().setProgress("VariablesAnnotationFailed", parser.getTableReference(), count, percent);
    }

    if (progressCount == cartVariableItemsMap.keySet().size()) {
      getView().setBusy(false);
      if (errorCount == 0) getView().hide();
    }
  }

  public interface Display extends PopupView, HasUiHandlers<CartVariableAttributeModalUiHandlers> {

    void setMode(boolean apply);

    void setTaxonomies(List<TaxonomyDto> taxonomies);

    void setBusy(boolean busy);

    void showError(String message);

    void setProgress(String messageKey, String tableRef, int count, int percent);

    void setLocales(JsArrayString locales);
  }

}
