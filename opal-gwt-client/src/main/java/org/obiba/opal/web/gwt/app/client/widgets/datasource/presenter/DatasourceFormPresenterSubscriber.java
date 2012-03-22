/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.datasource.presenter;


import com.google.web.bindery.event.shared.EventBus;

public abstract class DatasourceFormPresenterSubscriber implements RequestDatasourceFormsEvent.Handler {

  private final DatasourceFormPresenter dfp;

  protected DatasourceFormPresenterSubscriber(EventBus eventBus, DatasourceFormPresenter dfp) {
    this.dfp = dfp;
    eventBus.addHandler(RequestDatasourceFormsEvent.getType(), this);
  }

  @Override
  public void onRequestDatasourceFormsEvent(RequestDatasourceFormsEvent event) {
    event.getHasDatasourceForms().addDatasourceForm(dfp);
  }
}
