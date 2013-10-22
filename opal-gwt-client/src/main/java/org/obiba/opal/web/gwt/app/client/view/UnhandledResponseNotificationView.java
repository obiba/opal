package org.obiba.opal.web.gwt.app.client.view;

import org.obiba.opal.web.gwt.app.client.presenter.UnhandledResponseNotificationPresenter;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class UnhandledResponseNotificationView extends ViewImpl
    implements UnhandledResponseNotificationPresenter.Display {

  interface Binder extends UiBinder<Widget, UnhandledResponseNotificationView> {}

  @UiField
  Label errorMessage;

  @Inject
  public UnhandledResponseNotificationView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void setErrorMessage(String msg) {
    errorMessage.setText(msg);
  }

}
