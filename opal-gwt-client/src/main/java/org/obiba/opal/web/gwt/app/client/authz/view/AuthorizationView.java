/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.authz.view;

import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.authz.presenter.SubjectAuthorizationPresenter.Display;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class AuthorizationView extends Composite implements AuthorizationPresenter.Display {

  @UiField
  Label explanation;

  @UiField
  SimplePanel users;

  @UiField
  SimplePanel groups;

  private Display usersDisplay;

  private Display groupsDisplay;

  //
  // Static Variables
  //

  private static AuthorizationViewUiBinder uiBinder = GWT.create(AuthorizationViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  //
  // Constructors
  //

  public AuthorizationView() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  //
  // WidgetDisplay methods
  //

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  //
  // UiBinder
  //

  @UiTemplate("AuthorizationView.ui.xml")
  interface AuthorizationViewUiBinder extends UiBinder<Widget, AuthorizationView> {
  }

  @Override
  public void clear() {
    usersDisplay.clear();
    groupsDisplay.clear();
  }

  @Override
  public void setExplanation(String text) {
    explanation.setText(text);
    explanation.setVisible(text != null && text.length() > 0);
  }

  @Override
  public void setUserAuthorizationDisplay(Display display) {
    this.usersDisplay = display;
    users.clear();
    users.add(display.asWidget());
  }

  @Override
  public void setGroupAuthorizationDisplay(Display display) {
    this.groupsDisplay = display;
    groups.clear();
    groups.add(display.asWidget());
  }

}
