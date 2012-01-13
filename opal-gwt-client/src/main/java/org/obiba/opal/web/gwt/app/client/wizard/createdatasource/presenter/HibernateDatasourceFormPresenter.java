/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter;

import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.HibernateDatasourceFactoryDto;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;

/**
 *
 */
public class HibernateDatasourceFormPresenter extends PresenterWidget<DatasourceFormPresenter.Display> implements DatasourceFormPresenter {

  public static class Subscriber extends DatasourceFormPresenterSubscriber {

    @Inject
    public Subscriber(com.google.gwt.event.shared.EventBus eventBus, HibernateDatasourceFormPresenter presenter) {
      super(eventBus, presenter);
    }

  }

  @Inject
  public HibernateDatasourceFormPresenter(final Display display, final EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  public PresenterWidget<? extends Display> getPresenter() {
    return this;
  }

  @Override
  public DatasourceFactoryDto getDatasourceFactory() {
    HibernateDatasourceFactoryDto extensionDto = HibernateDatasourceFactoryDto.create();

    DatasourceFactoryDto dto = DatasourceFactoryDto.create();
    dto.setExtension(HibernateDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, extensionDto);

    return dto;
  }

  @Override
  public boolean isForType(String type) {
    return type.equalsIgnoreCase("hibernate");
  }

  @Override
  public boolean validateFormData() {
    return true;
  }

  @Override
  public void clearForm() {
  }

}
