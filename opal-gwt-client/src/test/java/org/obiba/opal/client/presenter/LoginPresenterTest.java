/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.client.presenter;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import net.customware.gwt.presenter.client.EventBus;

import org.junit.Test;
import org.obiba.opal.web.gwt.app.client.presenter.LoginPresenter;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;

public class LoginPresenterTest {

  @Test
  public void testThatClickHandlerIsAddedToSignIn() {

    LoginPresenter.Display displayMock = createMock(LoginPresenter.Display.class);
    EventBus eventBus = createMock(EventBus.class);
    RequestCredentials credentials = new RequestCredentials();
    ResourceRequestBuilderFactory factory = new ResourceRequestBuilderFactory(eventBus, credentials);

    LoginPresenter loginPresenter = new LoginPresenter(displayMock, eventBus, factory, credentials);

    HasClickHandlers hasClickHandlerMock = createMock(HasClickHandlers.class);
    expect(displayMock.getSignIn()).andReturn(hasClickHandlerMock).atLeastOnce();

    HandlerRegistration handlerRegistrationMock = createMock(HandlerRegistration.class);
    expect(hasClickHandlerMock.addClickHandler((ClickHandler) anyObject())).andReturn(handlerRegistrationMock).atLeastOnce();

    replay(displayMock, hasClickHandlerMock);

    loginPresenter.bind();

    verify(displayMock, hasClickHandlerMock);

  }

}
