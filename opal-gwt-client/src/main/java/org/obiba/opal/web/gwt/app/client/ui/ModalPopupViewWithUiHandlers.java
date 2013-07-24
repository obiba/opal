package org.obiba.opal.web.gwt.app.client.ui;

import com.google.web.bindery.event.shared.EventBus;

public abstract class ModalPopupViewWithUiHandlers <C extends com.gwtplatform.mvp.client.UiHandlers> extends ModalViewImpl implements com.gwtplatform.mvp.client.HasUiHandlers<C> {
  private C uiHandlers;

  protected ModalPopupViewWithUiHandlers(EventBus eventBus) {
    super(eventBus);
  }

  protected C getUiHandlers() {
    return uiHandlers;
  }

  @Override
  public void setUiHandlers(C uiHandlers) {
    this.uiHandlers = uiHandlers;
  }

}


