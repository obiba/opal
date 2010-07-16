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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import net.customware.gwt.presenter.client.EventBus;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.web.gwt.app.client.presenter.DataImportPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.event.FileSelectionEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.FileSelectionUpdateEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;
import org.obiba.opal.web.gwt.test.AbstractGwtTestSetup;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.GwtEvent.Type;

public class DataImportPresenterTest extends AbstractGwtTestSetup {

  private EventBus eventBusMock;

  private DataImportPresenter importPresenter;

  private DataImportPresenter.Display displayMock;

  private FileSelectionPresenter.Display fileSelectionDisplayMock;

  private FileSelectionPresenter fileSelectionPresenter;

  private FileSelectionPresenter.Display archiveSelectionDisplayMock;

  private FileSelectionPresenter archiveSelectionPresenter;

  @Before
  public void setUp() {
    displayMock = createMock(DataImportPresenter.Display.class);
    eventBusMock = createMock(EventBus.class);

    fileSelectionDisplayMock = createMock(FileSelectionPresenter.Display.class);
    fileSelectionPresenter = new FileSelectionPresenter(fileSelectionDisplayMock, eventBusMock);

    archiveSelectionDisplayMock = createMock(FileSelectionPresenter.Display.class);
    archiveSelectionPresenter = new FileSelectionPresenter(archiveSelectionDisplayMock, eventBusMock);

    importPresenter = new DataImportPresenter(displayMock, eventBusMock, fileSelectionPresenter, archiveSelectionPresenter);
  }

  @Test
  public void testThatEventHandlersAreAddedToUIComponents() {

    HandlerRegistration handlerRegistrationMock = createMock(HandlerRegistration.class);

    // Make sure that handlers are added to the event bus
    expect(eventBusMock.addHandler((Type<FileSelectionEvent.Handler>) EasyMock.anyObject(), (FileSelectionEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).times(2);
    expect(eventBusMock.addHandler((Type<FileSelectionUpdateEvent.Handler>) EasyMock.anyObject(), (FileSelectionUpdateEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();

    // Make sure that a ClickHandler is added to the Submit button
    expect(displayMock.addSubmitClickHandler((ClickHandler) EasyMock.anyObject())).andReturn(handlerRegistrationMock);
    expect(displayMock.addJobLinkClickHandler((ClickHandler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).atLeastOnce();

    expect(fileSelectionDisplayMock.addBrowseClickHandler((ClickHandler) EasyMock.anyObject())).andReturn(handlerRegistrationMock);
    expect(archiveSelectionDisplayMock.addBrowseClickHandler((ClickHandler) EasyMock.anyObject())).andReturn(handlerRegistrationMock);

    displayMock.setFileWidgetDisplay(fileSelectionDisplayMock);
    displayMock.setArchiveWidgetDisplay(archiveSelectionDisplayMock);

    // Expect that the presenter makes these calls to the server when it binds itself.
    ResourceRequestBuilder mockRequestBuilder = mockBridge.addMock(ResourceRequestBuilder.class);
    expect(mockRequestBuilder.get()).andReturn(mockRequestBuilder).anyTimes();
    expect(mockRequestBuilder.withCallback((ResourceCallback) EasyMock.anyObject())).andReturn(mockRequestBuilder).anyTimes();
    expect(mockRequestBuilder.forResource("/datasources")).andReturn(mockRequestBuilder).once();
    expect(mockRequestBuilder.forResource("/functional-units")).andReturn(mockRequestBuilder).once();
    mockRequestBuilder.send();
    EasyMock.expectLastCall().anyTimes();

    replay(eventBusMock, displayMock, fileSelectionDisplayMock, archiveSelectionDisplayMock, mockRequestBuilder);

    importPresenter.bind();

    verify(eventBusMock, displayMock, fileSelectionDisplayMock, archiveSelectionDisplayMock, mockRequestBuilder);

  }
}
