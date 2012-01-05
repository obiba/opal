/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.presenter;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import net.customware.gwt.presenter.client.EventBus;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSystemTreePresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileUploadDialogPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.event.FileSelectionEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.SelectButtonHandler;
import org.obiba.opal.web.gwt.rest.client.RequestUrlBuilder;
import org.obiba.opal.web.gwt.test.AbstractGwtTestSetup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.shared.testing.CountingEventBus;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Unit tests for {@link FileSelectorPresenter}.
 */
public class FileSelectorPresenterTest extends AbstractGwtTestSetup {
  //
  // Instance Variables
  //

  private EventBus eventBusMock;

  private CountingEventBus countingEventBus;

  private FileSelectorPresenter.Display displayMock;

  private FileSystemTreePresenterSpy fileSystemTreePresenter;

  private FolderDetailsPresenterSpy folderDetailsPresenter;

  private FileUploadDialogPresenterSpy fileUploadPresenter;

  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() {
    eventBusMock = createMock(EventBus.class);
    countingEventBus = new CountingEventBus();
    displayMock = createDisplay();
    fileSystemTreePresenter = createFileSystemTreePresenter(eventBusMock);
    folderDetailsPresenter = createFolderDetailsPresenter(eventBusMock);
    fileUploadPresenter = new FileUploadDialogPresenterSpy(null, eventBusMock, null);

    displayMock.setTreeDisplay(fileSystemTreePresenter.getDisplay());
    expectLastCall().once();
    displayMock.setDetailsDisplay(folderDetailsPresenter.getDisplay());
    expectLastCall().once();

    folderDetailsPresenter.getDisplay().setSelectionEnabled(true);
    expectLastCall().atLeastOnce();
  }

  //
  // Test Methods
  //

  @Test
  public void testConstructor() {
    // Setup
    replay(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());

    // Exercise
    new FileSelectorPresenter(displayMock, countingEventBus, fileSystemTreePresenter, folderDetailsPresenter, fileUploadPresenter);

    // Verify
    verify(eventBusMock, displayMock, folderDetailsPresenter.getDisplay(), folderDetailsPresenter.getDisplay());
  }

  @Test
  public void testOnBind() {
    // Setup
    expect(displayMock.addSelectButtonHandler((ClickHandler) anyObject())).andReturn(null).once();
    expect(displayMock.addCancelButtonHandler((ClickHandler) anyObject())).andReturn(null).once();
    expect(displayMock.addCreateFolderButtonHandler((ClickHandler) anyObject())).andReturn(null).once();
    expect(displayMock.addUploadButtonHandler((ClickHandler) anyObject())).andReturn(null).once();

    replay(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());

    // Exercise
    FileSelectorPresenter sut = new FileSelectorPresenter(displayMock, countingEventBus, fileSystemTreePresenter, folderDetailsPresenter, fileUploadPresenter);
    sut.onBind();

    // Verify
    assertEquals(1, fileSystemTreePresenter.getBindCount());
    assertEquals(1, folderDetailsPresenter.getBindCount());
    assertEquals(1, fileUploadPresenter.getBindCount());
    verify(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());
  }

  @Test
  public void testRevealDisplay() {
    // Setup
    folderDetailsPresenter.getDisplay().clearSelection();
    expectLastCall().once();

    folderDetailsPresenter.getDisplay().setDisplaysFiles(true);
    expectLastCall().once();

    displayMock.clearNewFileName();
    expectLastCall().once();

    displayMock.clearNewFolderName();
    expectLastCall().once();

    displayMock.setNewFilePanelVisible(true);
    expectLastCall().once();

    displayMock.setNewFolderPanelVisible(true);
    expectLastCall().once();

    displayMock.showDialog();
    expectLastCall().once();

    displayMock.setDisplaysUploadFile(true);
    expectLastCall().once();

    replay(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());

    // Exercise
    FileSelectorPresenter sut = new FileSelectorPresenter(displayMock, countingEventBus, fileSystemTreePresenter, folderDetailsPresenter, fileUploadPresenter);
    sut.onReveal();

    // Verify
    assertEquals(1, fileSystemTreePresenter.getRevealDisplayCount());
    assertEquals(1, folderDetailsPresenter.getRevealDisplayCount());
    verify(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());
  }

  @Test
  public void testRefreshDisplay() {
    // Setup
    replay(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());

    // Exercise
    FileSelectorPresenter sut = new FileSelectorPresenter(displayMock, countingEventBus, fileSystemTreePresenter, folderDetailsPresenter, fileUploadPresenter);
    sut.onReset();

    // Verify
    assertEquals(1, fileSystemTreePresenter.getRefreshDisplayCount());
    verify(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());
  }

  @Test
  public void testDisplaysFiles_ReturnsTrueWhenSelectingFile() {
    testDisplaysFiles(FileSelectionType.FILE, true);
  }

  @Test
  public void testDisplaysFiles_ReturnsTrueWhenSelectingExistingFile() {
    testDisplaysFiles(FileSelectionType.EXISTING_FILE, true);
  }

  @Test
  public void testDisplaysFiles_ReturnsFalseWhenSelectingFolder() {
    testDisplaysFiles(FileSelectionType.FOLDER, false);
  }

  @Test
  public void testDisplaysFiles_ReturnsFalseWhenSelectingExistingFolder() {
    testDisplaysFiles(FileSelectionType.EXISTING_FOLDER, false);
  }

  @Test
  public void testDisplaysFiles_ReturnsTrueWhenSelectingExistingFileOrFolder() {
    testDisplaysFiles(FileSelectionType.EXISTING_FILE_OR_FOLDER, true);
  }

  @Test
  public void testAllowsFileCreation_ReturnsTrueWhenSelectingFile() {
    testAllowsFileCreation(FileSelectionType.FILE, true);
  }

  @Test
  public void testAllowsFileCreation_ReturnsFalseWhenSelectingExistingFile() {
    testAllowsFileCreation(FileSelectionType.EXISTING_FILE, false);
  }

  @Test
  public void testAllowsFileCreation_ReturnsFalseWhenSelectingFolder() {
    testAllowsFileCreation(FileSelectionType.FOLDER, false);
  }

  @Test
  public void testAllowsFileCreation_ReturnsFalseWhenSelectingExistingFolder() {
    testAllowsFileCreation(FileSelectionType.EXISTING_FOLDER, false);
  }

  @Test
  public void testAllowsFileCreation_ReturnsFalseWhenSelectingExistingFileOrFolder() {
    testAllowsFileCreation(FileSelectionType.EXISTING_FILE_OR_FOLDER, false);
  }

  @Test
  public void testAllowsFolderCreation_ReturnsTrueWhenSelectingFile() {
    testAllowsFolderCreation(FileSelectionType.FILE, true);
  }

  @Test
  public void testAllowsFolderCreation_ReturnsFalseWhenSelectingExistingFile() {
    testAllowsFolderCreation(FileSelectionType.EXISTING_FILE, false);
  }

  @Test
  public void testAllowsFolderCreation_ReturnsTrueWhenSelectingFolder() {
    testAllowsFolderCreation(FileSelectionType.FOLDER, true);
  }

  @Test
  public void testAllowsFolderCreation_ReturnsFalseWhenSelectingExistingFolder() {
    testAllowsFolderCreation(FileSelectionType.EXISTING_FOLDER, false);
  }

  @Test
  public void testAllowsFolderCreation_ReturnsFalseWhenSelectingExistingFileOrFolder() {
    testAllowsFolderCreation(FileSelectionType.EXISTING_FILE_OR_FOLDER, false);
  }

  @Ignore("No longer testable due to usage of FileDto in presenter")
  @Test
  public void testOnSelectButtonClicked_FiresFileSelectionEvent() {
    // Setup
    expect(displayMock.getNewFileName()).andReturn(null).atLeastOnce();

    eventBusMock.fireEvent(isA(FileSelectionEvent.class));
    expectLastCall().once();

    displayMock.hideDialog();
    expectLastCall().once();

    expect(folderDetailsPresenter.getDisplay().getTableSelectionModel()).andReturn(new SingleSelectionModel());

    replay(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());

    FileSelectorPresenter presenter = new FileSelectorPresenter(displayMock, countingEventBus, fileSystemTreePresenter, folderDetailsPresenter, fileUploadPresenter);
    presenter.setFileSelectionType(FileSelectionType.EXISTING_FILE);

    // Exercise
    SelectButtonHandler sut = presenter.new SelectButtonHandler();
    sut.onClick(new DummyClickEvent());

    // Verify
    verify(eventBusMock, displayMock);
  }

  //
  // Helper Methods
  //

  private FileSelectorPresenter.Display createDisplay() {
    HasWidgets fileSystemTreePanelMock = createMock(HasWidgets.class);
    HasText createFolderNameMock = createMock(HasText.class);

    FileSelectorPresenter.Display displayMock = createMock(FileSelectorPresenter.Display.class);
    expect(displayMock.getCreateFolderName()).andReturn(createFolderNameMock).anyTimes();

    replay(fileSystemTreePanelMock);

    return displayMock;
  }

  @SuppressWarnings("unchecked")
  private FileSystemTreePresenterSpy createFileSystemTreePresenter(EventBus eventBus) {
    HasSelectionHandlers<TreeItem> fileSystemTreeMock = createNiceMock(HasSelectionHandlers.class);
    FileSystemTreePresenter.Display treeDisplayMock = createMock(FileSystemTreePresenter.Display.class);
    expect(treeDisplayMock.getFileSystemTree()).andReturn(fileSystemTreeMock);
    expect(treeDisplayMock.asWidget()).andReturn(null).atLeastOnce();

    replay(fileSystemTreeMock, treeDisplayMock);

    return new FileSystemTreePresenterSpy(treeDisplayMock, eventBus);
  }

  private FolderDetailsPresenterSpy createFolderDetailsPresenter(EventBus eventBus) {
    FolderDetailsPresenter.Display detailsDisplayMock = createMock(FolderDetailsPresenter.Display.class);

    return new FolderDetailsPresenterSpy(detailsDisplayMock, eventBus);
  }

  private void testDisplaysFiles(FileSelectionType fileSelectionType, boolean shouldDisplayFiles) {
    // Setup
    replay(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());

    FileSelectorPresenter sut = new FileSelectorPresenter(displayMock, countingEventBus, fileSystemTreePresenter, folderDetailsPresenter, fileUploadPresenter);
    sut.setFileSelectionType(fileSelectionType);

    // Exercise
    boolean displaysFiles = sut.displaysFiles();

    // Verify
    assertEquals(shouldDisplayFiles, displaysFiles);
  }

  private void testAllowsFileCreation(FileSelectionType fileSelectionType, boolean shouldAllowFileCreation) {
    // Setup
    replay(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());

    FileSelectorPresenter sut = new FileSelectorPresenter(displayMock, countingEventBus, fileSystemTreePresenter, folderDetailsPresenter, fileUploadPresenter);
    sut.setFileSelectionType(fileSelectionType);

    // Exercise
    boolean allowsFileCreation = sut.allowsFileCreation();

    // Verify
    assertEquals(shouldAllowFileCreation, allowsFileCreation);
  }

  private void testAllowsFolderCreation(FileSelectionType fileSelectionType, boolean shouldAllowFolderCreation) {
    // Setup
    replay(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());

    FileSelectorPresenter sut = new FileSelectorPresenter(displayMock, countingEventBus, fileSystemTreePresenter, folderDetailsPresenter, fileUploadPresenter);
    sut.setFileSelectionType(fileSelectionType);

    // Exercise
    boolean allowsFolderCreation = sut.allowsFolderCreation();

    // Verify
    assertEquals(shouldAllowFolderCreation, allowsFolderCreation);
  }

  //
  // Inner Classes
  //

  private static class FileSystemTreePresenterSpy extends FileSystemTreePresenter {

    private int bindCount;

    private int revealDisplayCount;

    private int refreshDisplayCount;

    public FileSystemTreePresenterSpy(FileSystemTreePresenter.Display display, EventBus eventBus) {
      super(display, eventBus);
    }

    protected void onBind() {
      bindCount++;
    }

    public void revealDisplay() {
      revealDisplayCount++;
    }

    public void refreshDisplay() {
      refreshDisplayCount++;
    }

    public int getBindCount() {
      return bindCount;
    }

    public int getRevealDisplayCount() {
      return revealDisplayCount;
    }

    public int getRefreshDisplayCount() {
      return refreshDisplayCount;
    }
  }

  private static class FolderDetailsPresenterSpy extends FolderDetailsPresenter {

    private int bindCount;

    private int revealDisplayCount;

    public FolderDetailsPresenterSpy(FolderDetailsPresenter.Display display, EventBus eventBus) {
      super(display, eventBus);
    }

    protected void onBind() {
      bindCount++;
    }

    public void revealDisplay() {
      revealDisplayCount++;
    }

    public int getBindCount() {
      return bindCount;
    }

    public int getRevealDisplayCount() {
      return revealDisplayCount;
    }
  }

  private static class FileUploadDialogPresenterSpy extends FileUploadDialogPresenter {

    private int bindCount;

    private int revealDisplayCount;

    public FileUploadDialogPresenterSpy(FileUploadDialogPresenter.Display display, EventBus eventBus, RequestUrlBuilder urlBuilder) {
      super(display, eventBus, urlBuilder);
    }

    protected void onBind() {
      bindCount++;
    }

    public void revealDisplay() {
      revealDisplayCount++;
    }

    public int getBindCount() {
      return bindCount;
    }

    public int getRevealDisplayCount() {
      return revealDisplayCount;
    }
  }

  private static class DummyClickEvent extends ClickEvent {
  }
}
