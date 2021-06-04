/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.datashield.profiles.packages;

import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;
import org.obiba.opal.web.model.client.opal.EntryDto;
import org.obiba.opal.web.model.client.opal.r.RPackageDto;

import com.google.gwt.core.client.JsArray;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class DataShieldPackageModalPresenter extends ModalPresenterWidget<DataShieldPackageModalPresenter.Display> {

  @Inject
  public DataShieldPackageModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
  }

  public void displayPackage(RPackageDto dto) {
    JsArray<EntryDto> entries = dto.getDescriptionArray();

    getView().clearProperties();
    for(int i = 0; i < entries.length(); i++) {
      getView().addProperty(entries.get(i));
    }
  }

  //
  // Inner classes and interfaces
  //

  public interface Display extends PopupView, HasUiHandlers<ModalUiHandlers> {

    void addProperty(EntryDto dto);

    void clearProperties();
  }

}
