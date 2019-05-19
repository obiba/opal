/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.database.list;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class DatabaseAdministrationView extends ViewImpl implements DatabaseAdministrationPresenter.Display {

  interface Binder extends UiBinder<Widget, DatabaseAdministrationView> {}

  @UiField
  HasWidgets breadcrumbs;

  @UiField
  Panel idsPanel;

  @UiField
  Panel dataPanel;

  @Inject
  public DatabaseAdministrationView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if(slot == DatabaseAdministrationPresenter.Slot.IDENTIFIERS) {
      idsPanel.clear();
      idsPanel.add(content);
    } else {
      dataPanel.clear();
      dataPanel.add(content);
    }
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

}
