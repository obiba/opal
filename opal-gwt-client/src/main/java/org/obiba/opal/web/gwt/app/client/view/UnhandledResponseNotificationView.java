package org.obiba.opal.web.gwt.app.client.view;

import org.obiba.opal.web.gwt.app.client.presenter.UnhandledResponseNotificationPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class UnhandledResponseNotificationView extends PopupPanel implements UnhandledResponseNotificationPresenter.Display {
  @UiTemplate("UnhandledResponseNotificationView.ui.xml")
  interface UnhandledResponseNotificationViewUiBinder extends UiBinder<Widget, UnhandledResponseNotificationView> {
  }

  private static UnhandledResponseNotificationViewUiBinder uiBinder = GWT.create(UnhandledResponseNotificationViewUiBinder.class);

  @UiField
  Label titleMessage;

  @UiField
  Label detailMessage;

  @UiField
  Button okay;

  public UnhandledResponseNotificationView() {
    // Clicking outside of the popupPanel will not dismiss the panel, panel is modal
    super(false, true);
    add(uiBinder.createAndBindUi(this));
  }

  @Override
  public HasClickHandlers getOkay() {
    return okay;
  }

  @Override
  public Widget asWidget() {
    return null;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  @Override
  public void showPopup() {
    setGlassEnabled(true);
    center();
    show();
  }

  @Override
  public void closePopup() {
    hide();
  }

}
