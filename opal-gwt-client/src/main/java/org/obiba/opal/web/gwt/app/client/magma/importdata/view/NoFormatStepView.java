/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.importdata.view;

import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.NoFormatStepPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 *
 */
public class NoFormatStepView extends ViewImpl implements NoFormatStepPresenter.Display {

  interface Binder extends UiBinder<Widget, NoFormatStepView> {}

  @UiField
  Widget label;

  @Inject
  public NoFormatStepView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

}
