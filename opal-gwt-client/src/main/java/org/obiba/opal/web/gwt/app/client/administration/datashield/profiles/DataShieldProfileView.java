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

import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.NavPillsPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DataShieldProfileView extends ViewWithUiHandlers<DataShieldProfileUiHandlers> implements DataShieldProfilePresenter.Display {

  interface Binder extends UiBinder<Widget, DataShieldProfileView> {}

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  Panel packages;

  @UiField
  SimplePanel aggregatePanel;

  @UiField
  SimplePanel assignPanel;

  @UiField
  SimplePanel optionsPanel;

  @Inject
  public DataShieldProfileView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void addToSlot(Object slot, IsWidget content) {
    if(slot == DataShieldProfilePresenter.AggregateEnvironmentSlot) {
      aggregatePanel.clear();
      aggregatePanel.setWidget(content.asWidget());
    }
    if(slot == DataShieldProfilePresenter.AssignEnvironmentSlot) {
      assignPanel.clear();
      assignPanel.setWidget(content.asWidget());
    }
    if(slot == DataShieldProfilePresenter.PackageSlot) {
      packages.clear();
      packages.add(content);
    }
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if(slot == DataShieldProfilePresenter.OptionsSlot) {
      optionsPanel.clear();
      optionsPanel.add(content);
    }
  }

}
