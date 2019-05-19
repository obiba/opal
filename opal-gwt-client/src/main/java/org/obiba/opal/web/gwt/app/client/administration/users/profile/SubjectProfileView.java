/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.users.profile;

import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;

import com.github.gwtbootstrap.client.ui.Form;
import com.github.gwtbootstrap.client.ui.Paragraph;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class SubjectProfileView extends ViewWithUiHandlers<SubjectProfileUiHandlers>
    implements SubjectProfilePresenter.Display {

  interface Binder extends UiBinder<Widget, SubjectProfileView> {}

  @UiField
  Paragraph accountText;

  @UiField
  Form accountForm;

  @UiField
  FlowPanel bookmarks;

  private final TranslationMessages translationMessages;

  @Inject
  public SubjectProfileView(Binder uiBinder, TranslationMessages translationMessages) {
    this.translationMessages = translationMessages;
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void enableChangePassword(boolean enabled, String realm) {
    accountForm.setVisible(enabled);
    accountText
        .setText(enabled ? translationMessages.accountEditable() : translationMessages.accountNotEditable(realm));
  }

  @UiHandler("changePassword")
  public void onChangePassword(ClickEvent event) {
    getUiHandlers().onChangePassword();
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if(slot == SubjectProfilePresenter.BOOKMARKS) {
      bookmarks.clear();
      bookmarks.add(content);
    }
  }
}
