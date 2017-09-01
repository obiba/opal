/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.cart.edit;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import org.obiba.opal.web.gwt.app.client.cart.service.CartVariableItem;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.support.MagmaPath;
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

  private boolean apply;

  private int progressCount = 0;

  private int errorCount = 0;

  @Inject
  public CartVariableAttributeModalPresenter(EventBus eventBus, Display display) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  public void initialize(List<CartVariableItem> cartVariableItems, boolean apply) {
    this.apply = apply;
    getView().setMode(apply);
    renderTaxonomies();
    cartVariableItemsMap.clear();
    for (CartVariableItem item : cartVariableItems) {
      String tableRef = item.getTableReference();
      if (!cartVariableItemsMap.containsKey(tableRef))
        cartVariableItemsMap.put(tableRef, new ArrayList<CartVariableItem>());
      cartVariableItemsMap.get(tableRef).add(item);
    }
  }

  private void renderTaxonomies() {
    ResourceRequestBuilderFactory.<JsArray<TaxonomyDto>>newBuilder()
        .forResource(UriBuilders.SYSTEM_CONF_TAXONOMIES.create().build()).get()
        .withCallback(new ResourceCallback<JsArray<TaxonomyDto>>() {
          @Override
          public void onResource(Response response, JsArray<TaxonomyDto> resource) {
            getView().setTaxonomies(JsArrays.toList(resource));
          }
        }).send();
  }

  @Override
  public void onSubmit(String taxonomy, String vocabulary, String term) {
    getView().setBusy(true);
    progressCount = 0;
    for (String tableRef : cartVariableItemsMap.keySet()) {
      final MagmaPath.Parser parser = MagmaPath.Parser.parse(tableRef);
      UriBuilder uriBuilder = UriBuilders.DATASOURCE_TABLE_VARIABLES_ATTRIBUTE.create()
          .query("namespace", taxonomy)
          .query("name", vocabulary);
      if (apply) uriBuilder.query("value", term);
      ResourceRequestBuilder builder = ResourceRequestBuilderFactory.newBuilder()
          .forResource(uriBuilder.build(parser.getDatasource(), parser.getTable()))
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              incrementProgress(parser, response.getStatusCode());
            }
          }, Response.SC_BAD_REQUEST, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_FORBIDDEN, Response.SC_NOT_FOUND, Response.SC_OK);
      for (CartVariableItem item : cartVariableItemsMap.get(tableRef))
        builder.withFormBody("variable", item.getVariable());
      builder.put().send();
    }
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
  }

}
