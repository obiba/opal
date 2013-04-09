/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.widgets.datasource.view;

import org.obiba.opal.web.gwt.app.client.widgets.datasource.presenter.NullDatasourceFormPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 *
 */
public class NullDatasourceFormView extends ViewImpl implements NullDatasourceFormPresenter.Display {

  @UiTemplate("NullDatasourceFormView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, NullDatasourceFormView> {}

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final Widget widget;

  public NullDatasourceFormView() {
    widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

}
