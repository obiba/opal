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
import org.obiba.opal.web.gwt.app.client.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.event.FileSelectionEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.TableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableListPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;
import org.obiba.opal.web.gwt.test.AbstractGwtTestSetup;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.GwtEvent.Type;

public class DataExportPresenterTest extends AbstractGwtTestSetup {

  private EventBus eventBusMock;

  private DataExportPresenter.Display displayMock;

  private DataExportPresenter exportPresenter;

  private TableListPresenter.Display tableListDisplayMock;

  private TableListPresenter tableListPresenter;

  private FileSelectionPresenter.Display fileSelectionDisplayMock;

  private FileSelectionPresenter fileSelectionPresenter;

  @Before
  public void setUp() {
    displayMock = createMock(DataExportPresenter.Display.class);
    eventBusMock = createMock(EventBus.class);

    tableListDisplayMock = createMock(TableListPresenter.Display.class);
    tableListPresenter = new TableListPresenter(tableListDisplayMock, eventBusMock);

    fileSelectionDisplayMock = createMock(FileSelectionPresenter.Display.class);
    fileSelectionPresenter = new FileSelectionPresenter(fileSelectionDisplayMock, eventBusMock);

    exportPresenter = new DataExportPresenter(displayMock, eventBusMock, tableListPresenter, fileSelectionPresenter);
  }

  @Test
  public void testThatEventHandlersAreAddedToUIComponents() {

    HandlerRegistration handlerRegistrationMock = createMock(HandlerRegistration.class);

    // Make sure that handlers are added to the event bus
    expect(eventBusMock.addHandler((Type<TableSelectionEvent.Handler>) EasyMock.anyObject(), (TableSelectionEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();
    expect(eventBusMock.addHandler((Type<FileSelectionEvent.Handler>) EasyMock.anyObject(), (FileSelectionEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();

    // Make sure that a ClickHandler is added to the Submit button
    HasClickHandlers hasClickHandlerSubmitMock = createMock(HasClickHandlers.class);
    expect(displayMock.getSubmit()).andReturn(hasClickHandlerSubmitMock).atLeastOnce();
    expect(hasClickHandlerSubmitMock.addClickHandler((ClickHandler) anyObject())).andReturn(handlerRegistrationMock).atLeastOnce();

    HasClickHandlers hasClickHandlerRemoveMock = createMock(HasClickHandlers.class);
    expect(tableListDisplayMock.getRemoveWidget()).andReturn(hasClickHandlerRemoveMock);
    expect(hasClickHandlerRemoveMock.addClickHandler((ClickHandler) anyObject())).andReturn(handlerRegistrationMock).atLeastOnce();

    HasClickHandlers hasClickHandlerAddMock = createMock(HasClickHandlers.class);
    expect(tableListDisplayMock.getAddWidget()).andReturn(hasClickHandlerAddMock);
    expect(hasClickHandlerAddMock.addClickHandler((ClickHandler) anyObject())).andReturn(handlerRegistrationMock).atLeastOnce();

    displayMock.setTableWidgetDisplay(tableListDisplayMock);

    HasClickHandlers hasClickHandlerBrowseMock = createMock(HasClickHandlers.class);
    expect(fileSelectionDisplayMock.getBrowseWidget()).andReturn(hasClickHandlerBrowseMock).atLeastOnce();
    expect(hasClickHandlerBrowseMock.addClickHandler((ClickHandler) anyObject())).andReturn(handlerRegistrationMock).atLeastOnce();

    displayMock.setFileWidgetDisplay(fileSelectionDisplayMock);

    // Expects that the presenter makes these calls to the server when it binds itself
    ResourceRequestBuilder mockRequestBuilder = mockBridge.addMock(ResourceRequestBuilder.class);
    expect(mockRequestBuilder.forResource("/datasources")).andReturn(mockRequestBuilder).once();
    expect(mockRequestBuilder.get()).andReturn(mockRequestBuilder).anyTimes();
    expect(mockRequestBuilder.withCallback((ResourceCallback) EasyMock.anyObject())).andReturn(mockRequestBuilder).anyTimes();
    expect(mockRequestBuilder.forResource("/functional-units")).andReturn(mockRequestBuilder).once();
    mockRequestBuilder.send();
    EasyMock.expectLastCall().anyTimes();

    replay(eventBusMock, tableListDisplayMock, fileSelectionDisplayMock, displayMock, hasClickHandlerSubmitMock, hasClickHandlerRemoveMock, hasClickHandlerAddMock, hasClickHandlerBrowseMock, mockRequestBuilder);

    exportPresenter.bind();

    verify(eventBusMock, tableListDisplayMock, fileSelectionDisplayMock, displayMock, hasClickHandlerSubmitMock, hasClickHandlerRemoveMock, hasClickHandlerAddMock, hasClickHandlerBrowseMock, mockRequestBuilder);

  }
}
