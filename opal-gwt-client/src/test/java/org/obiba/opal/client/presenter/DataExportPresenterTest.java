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
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import net.customware.gwt.presenter.client.EventBus;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.FileSelectionEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.TableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableListPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.exportdata.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.test.AbstractGwtTestSetup;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;

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
    expect(eventBusMock.addHandler(eq(TableSelectionEvent.getType()), isA(TableSelectionEvent.Handler.class))).andReturn(handlerRegistrationMock).once();
    expect(eventBusMock.addHandler(eq(FileSelectionEvent.getType()), isA(FileSelectionEvent.Handler.class))).andReturn(handlerRegistrationMock).once();
    expect(eventBusMock.addHandler(eq(ConfirmationEvent.getType()), isA(ConfirmationEvent.Handler.class))).andReturn(handlerRegistrationMock).once();

    // Make sure that a ClickHandler is added to the Submit button
    expect(displayMock.addCancelClickHandler((ClickHandler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).atLeastOnce();
    expect(displayMock.addSubmitClickHandler((ClickHandler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).atLeastOnce();
    expect(displayMock.addFinishClickHandler((ClickHandler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).atLeastOnce();
    expect(displayMock.addJobLinkClickHandler((ClickHandler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).atLeastOnce();
    expect(tableListDisplayMock.addRemoveClickHandler((ClickHandler) EasyMock.anyObject())).andReturn(handlerRegistrationMock);
    expect(tableListDisplayMock.addAddClickHandler((ClickHandler) EasyMock.anyObject())).andReturn(handlerRegistrationMock);

    expect(fileSelectionDisplayMock.addBrowseClickHandler((ClickHandler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).atLeastOnce();

    displayMock.setTablesValidator((ValidationHandler) EasyMock.anyObject());
    displayMock.setDestinationValidator((ValidationHandler) EasyMock.anyObject());
    displayMock.setTableWidgetDisplay(tableListDisplayMock);
    expect(displayMock.getFileFormat()).andReturn("csv");
    displayMock.setFileWidgetDisplay(fileSelectionDisplayMock);
    expect(displayMock.addFileFormatChangeHandler((ChangeHandler) EasyMock.anyObject())).andReturn(handlerRegistrationMock);

    replay(eventBusMock, tableListDisplayMock, fileSelectionDisplayMock, displayMock);

    exportPresenter.bind();

    verify(eventBusMock, tableListDisplayMock, fileSelectionDisplayMock, displayMock);

  }
}
