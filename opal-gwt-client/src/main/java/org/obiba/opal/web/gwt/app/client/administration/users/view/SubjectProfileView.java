package org.obiba.opal.web.gwt.app.client.administration.users.view;

import org.obiba.opal.web.gwt.app.client.administration.users.presenter.SubjectProfilePresenter;
import org.obiba.opal.web.gwt.app.client.administration.users.presenter.SubjectProfileUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;

import com.github.gwtbootstrap.client.ui.Form;
import com.github.gwtbootstrap.client.ui.Paragraph;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class SubjectProfileView extends ViewWithUiHandlers<SubjectProfileUiHandlers> implements SubjectProfilePresenter.Display {

  interface Binder extends UiBinder<Widget, SubjectProfileView> {}

  private final TranslationMessages translationMessages;

  @UiField
  Paragraph accountText;

  @UiField
  Form accountForm;

  @Inject
  public SubjectProfileView(Binder uiBinder, TranslationMessages translationMessages) {
    initWidget(uiBinder.createAndBindUi(this));
    this.translationMessages = translationMessages;
  }

  @Override
  public void enableChangePassword(boolean enabled, String realm) {
    accountForm.setVisible(enabled);
    accountText.setText(enabled ? translationMessages.accountEditable() : translationMessages.accountNotEditable(realm));
  }

  @UiHandler("changePassword")
  public void onChangePassword(ClickEvent event) {
    getUiHandlers().onChangePassword();
  }
}
