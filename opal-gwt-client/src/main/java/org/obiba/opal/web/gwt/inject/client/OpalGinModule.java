/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.inject.client;

import net.customware.gwt.presenter.client.EventBus;

import org.obiba.opal.web.gwt.app.client.place.DefaultPlace;
import org.obiba.opal.web.gwt.app.client.place.OpalPlaceManager;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.UnhandledResponseNotificationPresenter;
import org.obiba.opal.web.gwt.app.client.view.ApplicationView;
import org.obiba.opal.web.gwt.app.client.view.NotificationView;
import org.obiba.opal.web.gwt.app.client.view.UnhandledResponseNotificationView;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ConfirmationPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.DatasourceSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ItemSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ScriptEvaluationPopupPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.SummaryTabPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableListPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ValueSequencePopupPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.view.ConfirmationView;
import org.obiba.opal.web.gwt.app.client.widgets.view.DatasourceSelectorView;
import org.obiba.opal.web.gwt.app.client.widgets.view.ItemSelectorView;
import org.obiba.opal.web.gwt.app.client.widgets.view.ScriptEvaluationPopupView;
import org.obiba.opal.web.gwt.app.client.widgets.view.SummaryTabView;
import org.obiba.opal.web.gwt.app.client.widgets.view.TableListView;
import org.obiba.opal.web.gwt.app.client.widgets.view.TableSelectorView;
import org.obiba.opal.web.gwt.app.client.widgets.view.ValueSequencePopupView;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.EvaluateScriptPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createview.view.EvaluateScriptView;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.ScriptEvaluationPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ScriptEvaluationView;
import org.obiba.opal.web.gwt.rest.client.DefaultRequestUrlBuilder;
import org.obiba.opal.web.gwt.rest.client.RequestUrlBuilder;

import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.gwtplatform.mvp.client.gin.DefaultModule;

/**
 *
 */
public class OpalGinModule extends AbstractPresenterModule {

  @Override
  protected void configure() {
    bind(EventBus.class).to(GwtEventBusAdaptor.class).in(Singleton.class);
    bind(RequestUrlBuilder.class).to(DefaultRequestUrlBuilder.class).in(Singleton.class);
    bind(UnhandledResponseNotificationPresenter.Display.class).to(UnhandledResponseNotificationView.class).in(Singleton.class);

    bindConstant().annotatedWith(DefaultPlace.class).to(Places.dashboard);

    install(new DefaultModule(OpalPlaceManager.class));
    bindPresenter(ApplicationPresenter.class, ApplicationPresenter.Display.class, ApplicationView.class, ApplicationPresenter.Proxy.class);
    bindSingletonPresenterWidget(NotificationPresenter.class, NotificationPresenter.Display.class, NotificationView.class);

    configureWidgets();

  }

  private void configureWidgets() {
    bind(TableSelectorPresenter.Display.class).to(TableSelectorView.class).in(Singleton.class);
    bind(TableListPresenter.Display.class).to(TableListView.class);
    bind(ConfirmationPresenter.Display.class).to(ConfirmationView.class).in(Singleton.class);
    bind(ScriptEvaluationPopupPresenter.Display.class).to(ScriptEvaluationPopupView.class).in(Singleton.class);
    bind(DatasourceSelectorPresenter.Display.class).to(DatasourceSelectorView.class);
    bind(EvaluateScriptPresenter.Display.class).to(EvaluateScriptView.class);
    bind(ItemSelectorPresenter.Display.class).to(ItemSelectorView.class);
    bind(SummaryTabPresenter.Display.class).to(SummaryTabView.class);
    bind(ScriptEvaluationPresenter.Display.class).to(ScriptEvaluationView.class);
    bindPresenterWidget(ValueSequencePopupPresenter.class, ValueSequencePopupPresenter.Display.class, ValueSequencePopupView.class);
  }

}
