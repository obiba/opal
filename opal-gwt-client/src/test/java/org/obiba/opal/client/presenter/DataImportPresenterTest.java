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
import org.obiba.opal.web.gwt.app.client.presenter.DataImportPresenter;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.shared.HandlerRegistration;

public class DataImportPresenterTest {

  @Test
  public void testThatEventHandlersAreAddedToComponents() {

    DataImportPresenter.Display displayMock = createMock(DataImportPresenter.Display.class);
    EventBus eventBus = createMock(EventBus.class);
    RequestCredentials credentials = new RequestCredentials();
    ResourceRequestBuilderFactory factory = new ResourceRequestBuilderFactory(eventBus, credentials);

    DataImportPresenter importPresenter = new DataImportPresenter(displayMock, eventBus, factory) {
      @Override
      protected void initDisplayComponents() {
      }
    };

    HasClickHandlers hasClickHandlerMock = createMock(HasClickHandlers.class);
    expect(displayMock.getSubmit()).andReturn(hasClickHandlerMock).atLeastOnce();

    HasCloseHandlers hasCloseHandlerMock = createMock(HasCloseHandlers.class);
    expect(displayMock.getDialogBox()).andReturn(hasCloseHandlerMock).atLeastOnce();

    HandlerRegistration handlerRegistrationMock = createMock(HandlerRegistration.class);

    // Make sure that a ClickHandler is added to the Submit button
    expect(hasClickHandlerMock.addClickHandler((ClickHandler) anyObject())).andReturn(handlerRegistrationMock).atLeastOnce();

    // Make sure that a CloseHandler is added to the Import dialog
    expect(hasCloseHandlerMock.addCloseHandler((CloseHandler) anyObject())).andReturn(handlerRegistrationMock).atLeastOnce();

    replay(displayMock, hasClickHandlerMock);

    importPresenter.bind();

    verify(displayMock, hasClickHandlerMock);

  }

}
