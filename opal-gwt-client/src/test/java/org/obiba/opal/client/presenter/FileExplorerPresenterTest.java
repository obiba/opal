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
import org.obiba.opal.web.gwt.app.client.fs.event.FileDeletedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSystemTreeFolderSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileUploadedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderRefreshedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.CreateFolderDialogPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileExplorerPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSystemTreePresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileUploadDialogPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter.FileSelectionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.FolderCreationEvent;
import org.obiba.opal.web.gwt.test.AbstractGwtTestSetup;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

public class FileExplorerPresenterTest extends AbstractGwtTestSetup {

  private EventBus eventBusMock;

  private FileExplorerPresenter.Display fileExplorerDisplayMock;

  private FileExplorerPresenter fileExplorerPresenter;

  private FileSystemTreePresenter.Display fileSystemTreeDisplayMock;

  private FileSystemTreePresenter fileSystemTreePresenter;

  private FolderDetailsPresenter.Display folderDetailsDisplayMock;

  private FolderDetailsPresenter folderDetailsPresenter;

  private FileUploadDialogPresenter.Display fileUploadDialogDisplayMock;

  private FileUploadDialogPresenter fileUploadDialogPresenter;

  private CreateFolderDialogPresenter.Display createFolderDialogDisplayMock;

  private CreateFolderDialogPresenter createFolderDialogPresenter;

  @Before
  public void setUp() {
    eventBusMock = createMock(EventBus.class);

    fileSystemTreeDisplayMock = createMock(FileSystemTreePresenter.Display.class);
    fileSystemTreePresenter = new FileSystemTreePresenter(fileSystemTreeDisplayMock, eventBusMock);

    folderDetailsDisplayMock = createMock(FolderDetailsPresenter.Display.class);
    folderDetailsPresenter = new FolderDetailsPresenter(folderDetailsDisplayMock, eventBusMock);

    fileUploadDialogDisplayMock = createMock(FileUploadDialogPresenter.Display.class);
    fileUploadDialogPresenter = new FileUploadDialogPresenter(fileUploadDialogDisplayMock, eventBusMock);

    createFolderDialogDisplayMock = createMock(CreateFolderDialogPresenter.Display.class);
    createFolderDialogPresenter = new CreateFolderDialogPresenter(createFolderDialogDisplayMock, eventBusMock);

    fileExplorerDisplayMock = createMock(FileExplorerPresenter.Display.class);
    fileExplorerPresenter = new FileExplorerPresenter(fileExplorerDisplayMock, eventBusMock, fileSystemTreePresenter, folderDetailsPresenter, fileUploadDialogPresenter, createFolderDialogPresenter);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testThatEventHandlersAreAddedToUIComponents() {

    HandlerRegistration handlerRegistrationMock = createMock(HandlerRegistration.class);

    // Make sure that the correct handlers are added to the event bus.
    expect(eventBusMock.addHandler(eq(FolderSelectionChangeEvent.getType()), isA(FolderSelectionChangeEvent.Handler.class))).andReturn(handlerRegistrationMock).once();
    expect(eventBusMock.addHandler(eq(FolderCreationEvent.getType()), isA(FolderCreationEvent.Handler.class))).andReturn(handlerRegistrationMock).times(2);
    expect(eventBusMock.addHandler(eq(FileDeletedEvent.getType()), isA(FileDeletedEvent.Handler.class))).andReturn(handlerRegistrationMock).once();
    expect(eventBusMock.addHandler(eq(FileSystemTreeFolderSelectionChangeEvent.getType()), isA(FileSystemTreeFolderSelectionChangeEvent.Handler.class))).andReturn(handlerRegistrationMock).times(2);
    expect(eventBusMock.addHandler(eq(FileUploadedEvent.getType()), isA(FileUploadedEvent.Handler.class))).andReturn(handlerRegistrationMock).once();
    expect(eventBusMock.addHandler(eq(FileSelectionChangeEvent.getType()), isA(FileSelectionChangeEvent.Handler.class))).andReturn(handlerRegistrationMock).once();
    expect(eventBusMock.addHandler(eq(FolderRefreshedEvent.getType()), isA(FolderRefreshedEvent.Handler.class))).andReturn(handlerRegistrationMock).once();
    expect(eventBusMock.addHandler(eq(ConfirmationEvent.getType()), isA(ConfirmationEvent.Handler.class))).andReturn(handlerRegistrationMock).once();

    // Make sure the action buttons of the file explorer have a click handler assign to each of them.
    HasClickHandlers clickHandlerMock = createMock(HasClickHandlers.class);
    expect(fileExplorerDisplayMock.getFileDeleteButton()).andReturn(clickHandlerMock).once();
    expect(fileExplorerDisplayMock.getFileDownloadButton()).andReturn(clickHandlerMock).once();
    expect(fileExplorerDisplayMock.getFileUploadButton()).andReturn(clickHandlerMock).once();
    expect(fileExplorerDisplayMock.getCreateFolderButton()).andReturn(clickHandlerMock).once();
    expect(clickHandlerMock.addClickHandler((ClickHandler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).times(4);

    // Insure the presence of component specific handlers.
    expect(fileSystemTreeDisplayMock.addFileSystemTreeOpenHandler((OpenHandler<TreeItem>) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();
    expect(folderDetailsDisplayMock.addFileSelectionHandler((FileSelectionHandler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();

    expect(folderDetailsDisplayMock.asWidget()).andReturn(new Widget()).atLeastOnce();
    expect(fileSystemTreeDisplayMock.asWidget()).andReturn(new Widget()).atLeastOnce();
    folderDetailsDisplayMock.setSelectionEnabled(true);
    EasyMock.expectLastCall().atLeastOnce();
    expect(fileExplorerDisplayMock.getFileSystemTree()).andReturn(createMock(HasWidgets.class)).atLeastOnce();
    expect(fileExplorerDisplayMock.getFolderDetailsPanel()).andReturn(createMock(HasWidgets.class)).atLeastOnce();
    expect(fileSystemTreeDisplayMock.getFileSystemTree()).andReturn(createMock(HasSelectionHandlers.class));
    expect(folderDetailsDisplayMock.getTableSelectionModel()).andReturn(new SingleSelectionModel<FileDto>()).atLeastOnce();

    replay(eventBusMock, fileExplorerDisplayMock, fileSystemTreeDisplayMock, folderDetailsDisplayMock, clickHandlerMock);

    fileExplorerPresenter.bind();

    verify(eventBusMock, fileExplorerDisplayMock, fileSystemTreeDisplayMock, folderDetailsDisplayMock, clickHandlerMock);

  }

}
