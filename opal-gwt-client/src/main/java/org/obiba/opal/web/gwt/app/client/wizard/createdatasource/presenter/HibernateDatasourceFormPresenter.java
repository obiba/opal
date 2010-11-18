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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.HibernateDatasourceFactoryDto;

import com.google.inject.Inject;

/**
 *
 */
public class HibernateDatasourceFormPresenter extends WidgetPresenter<DatasourceFormPresenter.Display> implements DatasourceFormPresenter {

  @Inject
  public HibernateDatasourceFormPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void onBind() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void onUnbind() {
    // TODO Auto-generated method stub

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
    return type.equalsIgnoreCase("Opal");
  }

  @Override
  public void refreshDisplay() {
    // TODO Auto-generated method stub

  }

  @Override
  public void revealDisplay() {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean validateFormData() {
    return true;
  }

  @Override
  public void clearForm() {
  }

}
