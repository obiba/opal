/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.index.presenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.ESCfgDto;
import org.obiba.opal.web.model.client.opal.ServiceCfgDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class IndexConfigurationPresenter extends PresenterWidget<IndexConfigurationPresenter.Display> {

  private Mode dialogMode;

  private boolean isEnabled;

  public enum Mode {
    UPDATE
  }

  @Inject
  public IndexConfigurationPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  protected void onReveal() {
    ResourceRequestBuilderFactory.<ServiceCfgDto>newBuilder().forResource("/service/search/cfg")
        .withCallback(new ResourceCallback<ServiceCfgDto>() {

          @Override
          public void onResource(Response response, ServiceCfgDto dto) {
            ESCfgDto cfg = (ESCfgDto) dto.getExtension("Opal.ESCfgDto.params");

            isEnabled = cfg.getEnabled();
            getView().setClusterName(cfg.getClusterName());
            getView().setIndexName(cfg.getIndexName());
            getView().setDataNode(cfg.getDataNode());
            getView().setNbShards(cfg.getShards());
            getView().setNbReplicas(cfg.getReplicas());
            getView().setSettings(cfg.getSettings());
          }
        }).get().send();
  }

  @Override
  protected void onBind() {
    setDialogMode(Mode.UPDATE);

    registerHandler(getView().getSaveButton().addClickHandler(new CreateOrUpdateMethodClickHandler()));

    registerHandler(getView().getCancelButton().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        getView().hideDialog();
      }
    }));
  }

  private void setDialogMode(Mode dialogMode) {
    this.dialogMode = dialogMode;
    getView().setDialogMode(dialogMode);
  }

  //
  // Inner classes and interfaces
  //
  public class CreateOrUpdateMethodClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      if(dialogMode == Mode.UPDATE) {
        updateConfig();
      }
    }

    private void updateConfig() {
      ServiceCfgDto dto = ServiceCfgDto.create();

      dto.setName("search");

      ESCfgDto config = ESCfgDto.create();
      config.setEnabled(isEnabled);
      config.setClusterName(getView().getClusterName().getText());
      config.setIndexName(getView().getIndexName().getText());
      config.setDataNode(getView().isDataNode().getValue());
      config.setShards(getView().getNbShards().intValue());
      config.setReplicas(getView().getNbReplicas().intValue());
      config.setSettings(getView().getSettings().getText());

      dto.setExtension("Opal.ESCfgDto.params", config);

      putESCfg(dto);

    }

    private void putESCfg(ServiceCfgDto dto) {
      CreateOrUpdateMethodCallBack callbackHandler = new CreateOrUpdateMethodCallBack(dto);
      ResourceRequestBuilderFactory.newBuilder().forResource("/service/search/cfg").put()//
          .withResourceBody(ServiceCfgDto.stringify(dto))//
          .withCallback(Response.SC_OK, callbackHandler).send();
    }

  }

  private class CreateOrUpdateMethodCallBack implements ResponseCodeCallback {

    ServiceCfgDto dto;

    public CreateOrUpdateMethodCallBack(ServiceCfgDto dto) {
      this.dto = dto;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      getView().hideDialog();
      if(!(response.getStatusCode() == Response.SC_OK)) {
        ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
        getEventBus().fireEvent(
            NotificationEvent.Builder.newNotification().error(error.getStatus()).args(error.getArgumentsArray())
                .build());
      }
    }
  }

  public interface Display extends PopupView {

    void hideDialog();

    void setDialogMode(Mode dialogMode);

    HasClickHandlers getSaveButton();

    HasClickHandlers getCancelButton();

    void setClusterName(String clusterName);

    HasText getClusterName();

    void setIndexName(String indexName);

    HasText getIndexName();

    void setSettings(String settings);

    HasText getSettings();

    void setDataNode(Boolean b);

    HasValue<Boolean> isDataNode();

    Number getNbShards();

    void setNbShards(int nb);

    Number getNbReplicas();

    void setNbReplicas(int nb);
  }

}
