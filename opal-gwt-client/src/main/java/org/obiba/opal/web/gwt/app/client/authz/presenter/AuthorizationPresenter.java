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

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.model.client.opal.Subject.SubjectType;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 *
 */
public class AuthorizationPresenter extends PresenterWidget<AuthorizationPresenter.Display> {

  private final SubjectAuthorizationPresenter userAuthzPresenter;

  private final SubjectAuthorizationPresenter groupAuthzPresenter;

  private final Translations translation;

  private String translationKey;

  @Inject
  public AuthorizationPresenter(Display display, EventBus eventBus, SubjectAuthorizationPresenter userAuthzPresenter, SubjectAuthorizationPresenter groupAuthzPresenter, Translations translation) {
    super(eventBus, display);
    this.userAuthzPresenter = userAuthzPresenter;
    this.groupAuthzPresenter = groupAuthzPresenter;
    this.translation = translation;
  }

  public void setAclRequest(String key, AclRequest.Builder... builders) {
    userAuthzPresenter.setAclRequest(SubjectType.USER, builders);
    groupAuthzPresenter.setAclRequest(SubjectType.GROUP, builders);
  }

  @Override
  public void onReveal() {
    userAuthzPresenter.revealDisplay();
    groupAuthzPresenter.revealDisplay();
    getView().setExplanation(translation.permissionExplanationMap().get(translationKey));
  }

  @Override
  protected void onBind() {
    userAuthzPresenter.onBind();
    getView().setUserAuthorizationDisplay(userAuthzPresenter.getDisplay());
    groupAuthzPresenter.onBind();
    getView().setGroupAuthorizationDisplay(groupAuthzPresenter.getDisplay());
  }

  @Override
  protected void onUnbind() {
    userAuthzPresenter.onUnbind();
    groupAuthzPresenter.onUnbind();
  }

  public interface Display extends View {

    void setUserAuthorizationDisplay(SubjectAuthorizationPresenter.Display display);

    void setGroupAuthorizationDisplay(SubjectAuthorizationPresenter.Display display);

    void setExplanation(String text);

    void clear();

  }

}
