/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.datashield.presenter;

import org.obiba.opal.web.model.client.opal.EntryDto;
import org.obiba.opal.web.model.client.opal.r.RPackageDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class DataShieldPackagePresenter extends PresenterWidget<DataShieldPackagePresenter.Display> {

  @Inject
  public DataShieldPackagePresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    registerHandler(getView().getCloseButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getView().hideDialog();
      }
    }));
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

  public interface Display extends PopupView {

    void hideDialog();

    HasClickHandlers getCloseButton();

    void addProperty(EntryDto dto);

    void clearProperties();
  }

}
