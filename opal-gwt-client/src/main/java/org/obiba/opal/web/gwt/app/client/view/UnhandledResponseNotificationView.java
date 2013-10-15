package org.obiba.opal.web.gwt.app.client.view;

import org.obiba.opal.web.gwt.app.client.presenter.UnhandledResponseNotificationPresenter;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewImpl;

public class UnhandledResponseNotificationView extends PopupViewImpl
    implements UnhandledResponseNotificationPresenter.Display {

  interface Binder extends UiBinder<Widget, UnhandledResponseNotificationView> {}

  private final PopupPanel popup = new PopupPanel(false, true);

  @UiField
  Label titleMessage;

  @UiField
  Label detailMessage;

  @UiField
  Label errorMessage;

  @UiField
  Button okay;

  @Inject
  public UnhandledResponseNotificationView(EventBus eventBus, Binder uiBinder) {
    super(eventBus);
    popup.add(uiBinder.createAndBindUi(this));
    popup.setGlassEnabled(true);
    popup.addStyleName("alert alert-error error-popup");
  }

  @Override
  public HasClickHandlers getOkay() {
    return okay;
  }

  @Override
  public Label getErrorMessage() {
    return errorMessage;
  }

  @Override
  public Widget asWidget() {
    return popup;
  }

}
