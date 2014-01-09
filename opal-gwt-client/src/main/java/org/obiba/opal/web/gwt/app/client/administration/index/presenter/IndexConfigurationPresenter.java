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

import org.obiba.opal.web.gwt.app.client.administration.index.event.TableIndicesRefreshEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.ESCfgDto;
import org.obiba.opal.web.model.client.opal.ServiceCfgDto;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class IndexConfigurationPresenter extends ModalPresenterWidget<IndexConfigurationPresenter.Display>
    implements IndexConfigurationUiHandlers {

  public interface Display extends PopupView, HasUiHandlers<IndexConfigurationUiHandlers> {

    void hideDialog();

    void setDialogMode(Mode dialogMode);

    void setClusterName(String clusterName);

    String getClusterName();

    void setSettings(String settings);

    String getSettings();

    Number getNbShards();

    void setNbShards(int nb);

    Number getNbReplicas();

    void setNbReplicas(int nb);
  }

  private Mode dialogMode;

  private boolean isEnabled;

  private boolean dataNode;

  private String indexName;

  public enum Mode {
    UPDATE
  }

  @Inject
  public IndexConfigurationPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  @Override
  public void save() {
    if(dialogMode == Mode.UPDATE) {
      updateConfig();
    }
  }

  @Override
  protected void onReveal() {
    ResourceRequestBuilderFactory.<ServiceCfgDto>newBuilder().forResource("/service/search/cfg")
        .withCallback(new ResourceCallback<ServiceCfgDto>() {

          @Override
          public void onResource(Response response, ServiceCfgDto dto) {
            ESCfgDto cfg = (ESCfgDto) dto.getExtension("Opal.ESCfgDto.params");

            isEnabled = cfg.getEnabled();
            dataNode = cfg.getDataNode();
            indexName = cfg.getIndexName();

            getView().setClusterName(cfg.getClusterName());
            getView().setNbShards(cfg.getShards());
            getView().setNbReplicas(cfg.getReplicas());
            getView().setSettings(cfg.getSettings());
          }
        }).get().send();
  }

  @Override
  protected void onBind() {
    setDialogMode(Mode.UPDATE);
  }

  private void setDialogMode(Mode dialogMode) {
    this.dialogMode = dialogMode;
    getView().setDialogMode(dialogMode);
  }

  private void updateConfig() {
    ServiceCfgDto dto = ServiceCfgDto.create();

    dto.setName("search");

    ESCfgDto config = ESCfgDto.create();
    config.setEnabled(isEnabled);
    config.setClusterName(getView().getClusterName());
    config.setIndexName(indexName);
    config.setDataNode(dataNode);
    config.setShards(getView().getNbShards().intValue());
    config.setReplicas(getView().getNbReplicas().intValue());
    config.setSettings(getView().getSettings());

    dto.setExtension("Opal.ESCfgDto.params", config);

    putESCfg(dto);

  }

  private void putESCfg(ServiceCfgDto dto) {
    ResourceRequestBuilderFactory.newBuilder().forResource("/service/search/cfg") //
        .withResourceBody(ServiceCfgDto.stringify(dto)) //
        .withCallback(Response.SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().hideDialog();
            getEventBus().fireEvent(new TableIndicesRefreshEvent());
          }
        }) //
        .withCallback(Response.SC_INTERNAL_SERVER_ERROR, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().hideDialog();
            getEventBus().fireEvent(new TableIndicesRefreshEvent());
            getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
          }
        }) //
        .put().send();
  }

}
