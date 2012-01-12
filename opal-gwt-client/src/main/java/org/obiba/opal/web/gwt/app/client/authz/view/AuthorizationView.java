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
import org.obiba.opal.web.gwt.app.client.i18n.Translations;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 *
 */
public class AuthorizationView extends ViewImpl implements AuthorizationPresenter.Display {

  @UiTemplate("AuthorizationView.ui.xml")
  interface AuthorizationViewUiBinder extends UiBinder<Widget, AuthorizationView> {
  }

  private static AuthorizationViewUiBinder uiBinder = GWT.create(AuthorizationViewUiBinder.class);

  private final Translations translation = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  Label explanation;

  @UiField
  SimplePanel users;

  @UiField
  SimplePanel groups;

  public AuthorizationView() {
    widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setInSlot(Object slot, Widget content) {
    HasWidgets panel = null;
    if(slot == Slots.User) {
      panel = this.users;
    } else if(slot == Slots.Group) {
      panel = this.groups;
    }
    if(panel != null) {
      panel.clear();
      if(content != null) {
        panel.add(content);
      }
    }
  }

  @Override
  public void setExplanation(String key) {
    String text = translation.permissionExplanationMap().get(key);
    explanation.setText(text);
    explanation.setVisible(text != null && text.length() > 0);
  }

}
