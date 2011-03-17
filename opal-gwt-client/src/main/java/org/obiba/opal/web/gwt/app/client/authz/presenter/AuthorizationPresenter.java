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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.model.client.opal.Subject.SubjectType;

import com.google.inject.Inject;

/**
 *
 */
public class AuthorizationPresenter extends WidgetPresenter<AuthorizationPresenter.Display> {

  private SubjectAuthorizationPresenter userAuthzPresenter;

  private SubjectAuthorizationPresenter groupAuthzPresenter;

  //
  // Constructors
  //

  @Inject
  public AuthorizationPresenter(Display display, EventBus eventBus, SubjectAuthorizationPresenter userAuthzPresenter, SubjectAuthorizationPresenter groupAuthzPresenter) {
    super(display, eventBus);
    this.userAuthzPresenter = userAuthzPresenter;
    this.groupAuthzPresenter = groupAuthzPresenter;
  }

  public void setAclRequest(AclRequest.Builder... builders) {
    userAuthzPresenter.setAclRequest(SubjectType.USER, builders);
    groupAuthzPresenter.setAclRequest(SubjectType.GROUP, builders);
  }

  //
  // WidgetPresenter methods
  //

  @Override
  public void refreshDisplay() {
    userAuthzPresenter.refreshDisplay();
    groupAuthzPresenter.refreshDisplay();
  }

  @Override
  public void revealDisplay() {
    userAuthzPresenter.revealDisplay();
    groupAuthzPresenter.revealDisplay();
  }

  @Override
  protected void onBind() {
    userAuthzPresenter.onBind();
    getDisplay().setUserAuthorizationDisplay(userAuthzPresenter.getDisplay());
    groupAuthzPresenter.onBind();
    getDisplay().setGroupAuthorizationDisplay(groupAuthzPresenter.getDisplay());
  }

  @Override
  protected void onUnbind() {
    userAuthzPresenter.onUnbind();
    groupAuthzPresenter.onUnbind();
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  //
  // Interface and inner classes
  //

  public interface Display extends WidgetDisplay {

    void setUserAuthorizationDisplay(SubjectAuthorizationPresenter.Display display);

    void setGroupAuthorizationDisplay(SubjectAuthorizationPresenter.Display display);

    void setExplanation(String text);

    void clear();

  }

}
