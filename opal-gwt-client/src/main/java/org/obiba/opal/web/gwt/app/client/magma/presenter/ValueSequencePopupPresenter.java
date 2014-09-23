/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.presenter;

import java.util.Arrays;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.event.GeoValueDisplayEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

/**
 *
 */
public class ValueSequencePopupPresenter extends ModalPresenterWidget<ValueSequencePopupPresenter.Display>
    implements ValueSequencePopupUiHandlers {

  private final ModalProvider<EntityModalPresenter> entityModalProvider;

  private TableDto table;

  private VariableDto variable;

  private String entityIdentifier;

  /**
   * @param eventBus
   * @param view
   * @param proxy
   */
  @Inject
  public ValueSequencePopupPresenter(EventBus eventBus, Display view,
      ModalProvider<EntityModalPresenter> entityModalProvider) {
    super(eventBus, view);
    getView().setUiHandlers(this);
    this.entityModalProvider = entityModalProvider.setContainer(this);
  }

  @Override
  protected void onBind() {
    super.onBind();
    getView().setValueSetFetcher(new ValueSetFetcherImpl());
    addHandler();
  }

  public void initialize(TableDto table, VariableDto variable, String entityIdentifier, boolean modal) {
    this.table = table;
    this.variable = variable;
    this.entityIdentifier = entityIdentifier;
    getView().initialize(table, variable, entityIdentifier, modal);
  }

  //
  // Private methods
  //

  private void addHandler() {
    registerHandler(getView().getButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        getView().hide();
      }
    }));
  }

  //
  // Inner classes and Interfaces
  //

  private final class ValueSetFetcherImpl implements ValueSetFetcher {
    @Override
    public void request(String filter) {
      if(filter == null || filter.isEmpty()) {
        requestValueSet(Arrays.asList(variable), filter);
      } else {
        requestVariablesAndValueSet(filter);
      }
    }

    private void requestVariablesAndValueSet(final String filter) {
      ResourceRequestBuilderFactory.<JsArray<VariableDto>>newBuilder()
          .forResource(table.getLink() + "/variables?script=" + URL.encodePathSegment(filter)).get()
          .withCallback(new ResourceCallback<JsArray<VariableDto>>() {

            @Override
            public void onResource(Response response, JsArray<VariableDto> resource) {
              requestValueSet(JsArrays.toList(resource), filter);
            }

          }).send();
    }

    private void requestValueSet(final List<VariableDto> variables, String filter) {
      StringBuilder link = new StringBuilder(table.getLink());
      link.append("/valueSet/").append(entityIdentifier).append("?select=");
      if(filter == null || filter.isEmpty()) {
        link.append(URL.encodePathSegment("name().eq('" + variable.getName() + "')"));
      } else {
        link.append(URL.encodePathSegment(filter));
      }
      ResourceRequestBuilderFactory.<ValueSetsDto>newBuilder().forResource(link.toString()).get()
          .withCallback(new ResourceCallback<ValueSetsDto>() {

            @Override
            public void onResource(Response response, ValueSetsDto resource) {
              getView().populate(variables, resource);
            }
          }).send();
    }

    @Override
    public void requestBinaryValue(VariableDto variable, String entityIdentifier, int index) {
      getEventBus().fireEvent(new FileDownloadRequestEvent(
          table.getLink() + "/valueSet/" + entityIdentifier + "/variable/" + variable.getName() + "/value?pos=" +
              index));
    }

    @Override
    public void requestGeoValue(VariableDto variable, String entityIdentifier, ValueSetsDto.ValueDto value, int index) {
      getEventBus().fireEvent(new GeoValueDisplayEvent(variable, entityIdentifier, value, index));
    }

    @Override
    public void requestEntityID(VariableDto variable, String entityIdentifier, ValueSetsDto.ValueDto value, int index) {
      String entityId = value.getValuesArray().get(index).getValue();
      EntityModalPresenter entityModalPresenter = entityModalProvider.get();
      entityModalPresenter.initialize(null, variable.getReferencedEntityType(), entityId, "");
    }
  }

  public interface Display extends PopupView, HasUiHandlers<ValueSequencePopupUiHandlers> {

    void initialize(TableDto table, VariableDto variable, String entityIdentifier, boolean modal);

    HasClickHandlers getButton();

    void populate(List<VariableDto> variables, ValueSetsDto valueSet);

    void setValueSetFetcher(ValueSetFetcher fetcher);

  }

  public interface ValueSetFetcher {

    void request(String filter);

    void requestBinaryValue(VariableDto variable, String entityIdentifier, int index);

    void requestGeoValue(VariableDto variable, String entityIdentifier, ValueSetsDto.ValueDto value, int index);

    void requestEntityID(VariableDto variable, String entityIdentifier, ValueSetsDto.ValueDto value, int index);
  }

}
