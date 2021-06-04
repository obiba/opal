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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import org.obiba.opal.web.gwt.app.client.administration.datashield.profiles.config.DataShieldMethodsConfigPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.profiles.config.DataShieldROptionsPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.profiles.packages.DataShieldPackagesPresenter;
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

  private final DataShieldPackagesPresenter packagePresenter;

  private final DataShieldMethodsConfigPresenter aggregatePresenter;

  private final DataShieldMethodsConfigPresenter assignPresenter;

  private final DataShieldROptionsPresenter optionsPresenter;

  private RServerClusterDto cluster;

  @Inject
  public DataShieldProfilePresenter(Display display, EventBus eventBus,
                                    Provider<ResourcePermissionsPresenter> resourcePermissionsProvider,
                                    Provider<DataShieldMethodsConfigPresenter> adminPresenterProvider,
                                    Provider<DataShieldROptionsPresenter> optionsProvider,
                                    DataShieldPackagesPresenter packagePresenter) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    this.resourcePermissionsProvider = resourcePermissionsProvider;
    this.packagePresenter = packagePresenter;
    this.optionsPresenter = optionsProvider.get();
    this.aggregatePresenter = adminPresenterProvider.get();
    this.aggregatePresenter.setEnvironment(DataShieldEnvironment.AGGREGATE);
    this.assignPresenter = adminPresenterProvider.get();
    this.assignPresenter.setEnvironment(DataShieldEnvironment.ASSIGN);
  }

  public void setCluster(RServerClusterDto cluster) {
    this.cluster = cluster;
    packagePresenter.setCluster(cluster);
    aggregatePresenter.setCluster(cluster);
    assignPresenter.setCluster(cluster);
    optionsPresenter.setCluster(cluster);
  }

  @Override
  protected void onBind() {
    addToSlot(PackageSlot, packagePresenter);
    setInSlot(OptionsSlot, optionsPresenter);
    addToSlot(AggregateEnvironmentSlot, aggregatePresenter);
    addToSlot(AssignEnvironmentSlot, assignPresenter);
  }

  public interface DataShieldEnvironment {

    String ASSIGN = "assign";

    String AGGREGATE = "aggregate";
  }
}
