/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.unit.presenter;

import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.unit.event.GenerateIdentifiersConfirmationEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

/**
 *
 */
public class GenerateIdentifiersModalPresenter extends ModalPresenterWidget<GenerateIdentifiersModalPresenter.Display>
    implements GenerateIdentifiersModalUiHandlers {

  //
  // Instance Variables
  //

  //
  // Constructors
  //

  @Inject
  public GenerateIdentifiersModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  public void setAffectedEntitiesCount(int affectedEntitiesCount) {
    getView().setAffectedEntities(affectedEntitiesCount);
  }

  @Override
  public void generateIdentifiers() {
    Display view = getView();
    getEventBus()
        .fireEvent(new GenerateIdentifiersConfirmationEvent(view.getSize(), view.getAllowZeros(), view.getPrefix()));
    view.hideDialog();
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends PopupView, HasUiHandlers<GenerateIdentifiersModalUiHandlers> {

    void hideDialog();

    void setAffectedEntities(int count);

    Number getSize();

    String getPrefix();

    boolean getAllowZeros();
  }
}
