/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.widgets.datasource.presenter;

import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;

/**
 *
 */
public class NullDatasourceFormPresenter extends PresenterWidget<NullDatasourceFormPresenter.Display>
    implements DatasourceFormPresenter {

  public static class Subscriber extends DatasourceFormPresenterSubscriber {

    @Inject
    public Subscriber(EventBus eventBus, NullDatasourceFormPresenter presenter) {
      super(eventBus, presenter);
    }

  }

  @Inject
  public NullDatasourceFormPresenter(final Display display, final EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  public PresenterWidget<? extends Display> getPresenter() {
    return this;
  }

  @Override
  public DatasourceFactoryDto getDatasourceFactory() {
    return DatasourceFactoryDto.create();
  }

  @Override
  public boolean isForType(String type) {
    return type.equalsIgnoreCase("null");
  }

  @Override
  public boolean validateFormData() {
    return true;
  }

  @Override
  public void clearForm() {
  }

  @Override
  protected void onReveal() {
  }

  public interface Display extends DatasourceFormPresenter.Display {

  }

}
