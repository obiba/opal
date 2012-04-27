/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.authz.presenter;

import org.obiba.opal.web.model.client.opal.Subject.SubjectType;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class AuthorizationPresenter extends PresenterWidget<AuthorizationPresenter.Display> {

  private final SubjectAuthorizationPresenter userAuthzPresenter;

  private final SubjectAuthorizationPresenter groupAuthzPresenter;

  @Inject
  public AuthorizationPresenter(Display display, EventBus eventBus, SubjectAuthorizationPresenter userAuthzPresenter, SubjectAuthorizationPresenter groupAuthzPresenter) {
    super(eventBus, display);
    this.userAuthzPresenter = userAuthzPresenter;
    this.groupAuthzPresenter = groupAuthzPresenter;
  }

  public void setAclRequest(String key, AclRequest... requests) {
    userAuthzPresenter.setAclRequest(SubjectType.USER, requests);
    groupAuthzPresenter.setAclRequest(SubjectType.GROUP, requests);
    getView().setExplanation(key);
  }

  @Override
  protected void onBind() {
    super.onBind();
    setInSlot(Display.Slots.User, userAuthzPresenter);
    setInSlot(Display.Slots.Group, groupAuthzPresenter);
  }

  public interface Display extends View {

    enum Slots {
      User, Group
    }

    void setExplanation(String key);

  }

}
