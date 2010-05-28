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

import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.web.gwt.app.client.event.NavigatorSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SelectionModel.SelectionChangeHandler;

public class DataExportPresenterTest {

  private EventBus eventBusMock;

  private DataExportPresenter.Display displayMock;

  private DataExportPresenter exportPresenter;

  @Before
  public void setUp() {
    displayMock = createMock(DataExportPresenter.Display.class);
    eventBusMock = createMock(EventBus.class);
    RequestCredentials credentials = new RequestCredentials();
    ResourceRequestBuilderFactory factory = new ResourceRequestBuilderFactory(eventBusMock, credentials);

    exportPresenter = new DataExportPresenter(displayMock, eventBusMock, factory) {
      @Override
      protected void initDisplayComponents() {
      }
    };
  }

  @Test
  public void testThatEventHandlersAreAddedToUIComponents() {

    HasSelectionHandlers hasSelectionHandlerMock = createMock(HasSelectionHandlers.class);
    expect(displayMock.getTableTree()).andReturn(hasSelectionHandlerMock).atLeastOnce();

    SelectionModel selectionModelMock = createMock(SelectionModel.class);
    expect(displayMock.getTableSelection()).andReturn(selectionModelMock).atLeastOnce();

    HasClickHandlers hasClickHandlerMock = createMock(HasClickHandlers.class);
    expect(displayMock.getSubmit()).andReturn(hasClickHandlerMock).atLeastOnce();

    HandlerRegistration handlerRegistrationMock = createMock(HandlerRegistration.class);

    // Make sure that a SelectionHandler is added to the browsing tree
    expect(hasSelectionHandlerMock.addSelectionHandler((SelectionHandler) anyObject())).andReturn(handlerRegistrationMock).atLeastOnce();

    // Make sure that a SelectionChangeHandler is added to the table
    expect(selectionModelMock.addSelectionChangeHandler((SelectionChangeHandler) anyObject())).andReturn(handlerRegistrationMock).atLeastOnce();

    // Make sure that a ClickHandler is added to the Submit button
    expect(hasClickHandlerMock.addClickHandler((ClickHandler) anyObject())).andReturn(handlerRegistrationMock).atLeastOnce();

    // Make sure that a NavigatorSelectionChangeEvent.Handler is added to the event bus
    NavigatorSelectionChangeEvent.Handler selectionChangeEventMock = createMock(NavigatorSelectionChangeEvent.Handler.class);
    expect(eventBusMock.addHandler(NavigatorSelectionChangeEvent.getType(), selectionChangeEventMock)).andReturn(handlerRegistrationMock).atLeastOnce();

    replay(displayMock, hasClickHandlerMock);

    exportPresenter.bind();

    verify(displayMock, hasClickHandlerMock);

  }
}
