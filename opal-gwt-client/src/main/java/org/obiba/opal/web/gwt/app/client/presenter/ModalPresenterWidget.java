package org.obiba.opal.web.gwt.app.client.presenter;

import org.obiba.opal.web.gwt.app.client.event.ModalClosedEvent;
import org.obiba.opal.web.gwt.app.client.event.ModalShownEvent;
import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class ModalPresenterWidget<V extends PopupView> extends PresenterWidget<V> implements ModalUiHandlers {

  public ModalPresenterWidget(EventBus eventBus, V view) {
    super(eventBus, view);
  }

  @Override
  public void onModalShown() {
    getEventBus().fireEventFromSource(new ModalShownEvent(), this);
  }

  @Override
  public void onModalHidden() {
    getEventBus().fireEventFromSource(new ModalClosedEvent(this), this);
  }
}
