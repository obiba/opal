/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.index.presenter;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.administration.index.event.TableIndicesRefreshEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.validator.ConditionValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.HasBooleanValue;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.ESCfgDto;
import org.obiba.opal.web.model.client.opal.ServiceCfgDto;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

import static org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexConfigurationPresenter.Display.FormField.CLUSTER_NAME;
import static org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexConfigurationPresenter.Display.FormField.REPLICAS;
import static org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexConfigurationPresenter.Display.FormField.SHARDS;
import static org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexConfigurationPresenter.Display.FormField.valueOf;

public class IndexConfigurationPresenter extends ModalPresenterWidget<IndexConfigurationPresenter.Display>
    implements IndexConfigurationUiHandlers {

  private final IndexConfValidationHandler validationHandler;

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
    validationHandler = new IndexConfValidationHandler();
  }

  @Override
  public void save(String clusterName, int nbShards, int nbReplicas, String settings) {
    getView().clearErrors();
    if(validationHandler.validate()) {

      ServiceCfgDto dto = ServiceCfgDto.create();

      dto.setName("search");

      ESCfgDto config = ESCfgDto.create();
      config.setEnabled(isEnabled);
      config.setClusterName(clusterName);
      config.setIndexName(indexName);
      config.setDataNode(dataNode);
      config.setShards(nbShards);
      config.setReplicas(nbReplicas);
      config.setSettings(settings);

      dto.setExtension("Opal.ESCfgDto.params", config);

      putESCfg(dto);
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
            getView().setConfiguration(cfg);
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

  public interface Display extends PopupView, HasUiHandlers<IndexConfigurationUiHandlers> {

    void setConfiguration(ESCfgDto cfg);

    enum FormField {
      CLUSTER_NAME,
      SHARDS,
      REPLICAS,
    }

    void hideDialog();

    void setDialogMode(Mode dialogMode);

    HasText getClusterName();

    HasText getSettings();

    Number getNbShards();

    Number getNbReplicas();

    void clearErrors();

    void showError(@Nullable FormField formField, String message);
  }

  private class IndexConfValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<FieldValidator>();
        validators
            .add(new RequiredTextValidator(getView().getClusterName(), "ClusterNameIsRequired", CLUSTER_NAME.name()));
        validators.add(new ConditionValidator(nbShardsIsNotEmpty(), "ShardsIsRequired", SHARDS.name()));
        validators.add(new ConditionValidator(nbReplicasIsNotEmpty(), "ReplicasIsRequired", REPLICAS.name()));
      }
      return validators;
    }

    private HasValue<Boolean> nbShardsIsNotEmpty() {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          return getView().getNbShards() != null && getView().getNbShards().intValue() >= 0;
        }
      };
    }

    private HasValue<Boolean> nbReplicasIsNotEmpty() {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          return getView().getNbReplicas() != null && getView().getNbReplicas().intValue() >= 0;
        }
      };
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(valueOf(id), message);
    }

  }

}
