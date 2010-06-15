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

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.web.gwt.app.client.presenter.DataImportPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;
import org.obiba.opal.web.gwt.test.AbstractGwtTestSetup;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;

public class DataImportPresenterTest extends AbstractGwtTestSetup {

  private DataImportPresenter importPresenter;

  private DataImportPresenter.Display displayMock;

  @Before
  public void setUp() {
    displayMock = createMock(DataImportPresenter.Display.class);
    EventBus eventBus = createMock(EventBus.class);
    importPresenter = new DataImportPresenter(displayMock, eventBus);
  }

  @Test
  public void testThatEventHandlersAreAddedToUIComponents() {

    HasClickHandlers hasClickHandlerMock = createMock(HasClickHandlers.class);
    expect(displayMock.getSubmit()).andReturn(hasClickHandlerMock).atLeastOnce();

    HandlerRegistration handlerRegistrationMock = createMock(HandlerRegistration.class);

    // Make sure that a ClickHandler is added to the Submit button
    expect(hasClickHandlerMock.addClickHandler((ClickHandler) anyObject())).andReturn(handlerRegistrationMock).atLeastOnce();

    // Expect that the presenter makes these calls to the server when it binds itself.
    ResourceRequestBuilder mockRequestBuilder = mockBridge.addMock(ResourceRequestBuilder.class);
    expect(mockRequestBuilder.forResource("/filesystem")).andReturn(mockRequestBuilder).once();
    expect(mockRequestBuilder.get()).andReturn(mockRequestBuilder).anyTimes();
    expect(mockRequestBuilder.withCallback((ResourceCallback) EasyMock.anyObject())).andReturn(mockRequestBuilder).anyTimes();
    expect(mockRequestBuilder.forResource("/datasources")).andReturn(mockRequestBuilder).once();
    expect(mockRequestBuilder.forResource("/functional-units")).andReturn(mockRequestBuilder).once();
    mockRequestBuilder.send();
    EasyMock.expectLastCall().anyTimes();

    replay(displayMock, hasClickHandlerMock, mockRequestBuilder);

    importPresenter.bind();

    verify(displayMock, hasClickHandlerMock, mockRequestBuilder);

  }
}
