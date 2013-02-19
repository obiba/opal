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

import org.obiba.opal.web.gwt.app.client.unit.event.GenerateIdentifiersConfirmationEvent;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

/**
 *
 */
public class GenerateIdentifiersDialogPresenter extends PresenterWidget<GenerateIdentifiersDialogPresenter.Display> {

  //
  // Instance Variables
  //

  //
  // Constructors
  //

  @Inject
  public GenerateIdentifiersDialogPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
    super.onBind();
    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    getView().clear();
  }

  public void setAffectedEntitiesCount(int affectedEntitiesCount) {
    getView().setAffectedEntities(affectedEntitiesCount);
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends PopupView {

    void hideDialog();

    void setAffectedEntities(int count);

    HasClickHandlers getGenerateIdentifiersButton();

    HasClickHandlers getCancelButton();

    Number getSize();

    String getPrefix();

    boolean getAllowZeros();

    void clear();
  }


  public class GetGenerateIdentifiersClickHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent event) {
      Display view = getView();
      getEventBus().fireEvent(new GenerateIdentifiersConfirmationEvent(view.getSize(), view.getAllowZeros(), view.getPrefix()));
      getView().hideDialog();
    }

  }

  //
  // Private members
  //

  private void addEventHandlers() {
    registerHandler(getView().getGenerateIdentifiersButton().addClickHandler(new GetGenerateIdentifiersClickHandler()));
    registerHandler(getView().getCancelButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getView().hideDialog();
      }
    }));
  }
}
