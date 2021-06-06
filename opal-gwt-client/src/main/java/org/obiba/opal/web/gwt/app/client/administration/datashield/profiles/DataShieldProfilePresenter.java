/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.datashield.profiles;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldProfileResetEvent;
import org.obiba.opal.web.gwt.app.client.administration.datashield.profiles.config.DataShieldMethodsPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.profiles.config.DataShieldROptionsPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.ResourcePermissionsPresenter;
import org.obiba.opal.web.model.client.opal.r.RServerClusterDto;

public class DataShieldProfilePresenter
    extends PresenterWidget<DataShieldProfilePresenter.Display>
    implements DataShieldProfileUiHandlers {

  public interface Display extends View, HasUiHandlers<DataShieldProfileUiHandlers> {


  }

  private final Provider<ResourcePermissionsPresenter> resourcePermissionsProvider;

  public static final Object PackageSlot = new Object();

  public static final Object AggregateEnvironmentSlot = new Object();

  public static final Object AssignEnvironmentSlot = new Object();

  public static final Object OptionsSlot = new Object();

  private final Provider<DataShieldMethodsPresenter> methodsPresenterProvider;

  private final Provider<DataShieldROptionsPresenter> optionsProvider;

  private RServerClusterDto cluster;

  @Inject
  public DataShieldProfilePresenter(Display display, EventBus eventBus,
                                    Provider<ResourcePermissionsPresenter> resourcePermissionsProvider,
                                    Provider<DataShieldMethodsPresenter> methodsPresenterProvider,
                                    Provider<DataShieldROptionsPresenter> optionsProvider) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    this.methodsPresenterProvider = methodsPresenterProvider;
    this.resourcePermissionsProvider = resourcePermissionsProvider;
    this.optionsProvider = optionsProvider;
  }

  @Override
  public void onReset() {
    fireEvent(new DataShieldProfileResetEvent(cluster.getName()));
  }

  public void setCluster(RServerClusterDto cluster) {
    this.cluster = cluster;
    DataShieldMethodsPresenter assignPresenter = methodsPresenterProvider.get();
    assignPresenter.setEnvironment(DataShieldEnvironment.ASSIGN);
    assignPresenter.setCluster(cluster);
    addToSlot(AssignEnvironmentSlot, assignPresenter);

    DataShieldMethodsPresenter aggregatePresenter = methodsPresenterProvider.get();
    aggregatePresenter.setEnvironment(DataShieldEnvironment.AGGREGATE);
    aggregatePresenter.setCluster(cluster);
    addToSlot(AggregateEnvironmentSlot, aggregatePresenter);

    DataShieldROptionsPresenter optionsPresenter = optionsProvider.get();
    optionsPresenter.setCluster(cluster);
    addToSlot(OptionsSlot, optionsPresenter);
  }

  @Override
  protected void onBind() {
  }

  public interface DataShieldEnvironment {

    String ASSIGN = "assign";

    String AGGREGATE = "aggregate";
  }
}
