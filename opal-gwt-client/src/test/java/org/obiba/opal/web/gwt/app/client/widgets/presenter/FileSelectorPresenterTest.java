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
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import net.customware.gwt.presenter.client.EventBus;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSystemTreeFolderSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSystemTreePresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter.FileSelectionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.event.FileSelectionRequiredEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.test.AbstractGwtTestSetup;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * Unit tests for {@link FileSelectorPresenter}.
 */
public class FileSelectorPresenterTest extends AbstractGwtTestSetup {
  //
  // Instance Variables
  //

  private EventBus eventBusMock;

  private FileSelectorPresenter.Display displayMock;

  private FileSystemTreePresenterSpy fileSystemTreePresenter;

  private FolderDetailsPresenterSpy folderDetailsPresenter;

  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() {
    eventBusMock = createMock(EventBus.class);
    displayMock = createDisplay();
    fileSystemTreePresenter = createFileSystemTreePresenter(eventBusMock);
    folderDetailsPresenter = createFolderDetailsPresenter(eventBusMock);

    displayMock.setTreeDisplay(fileSystemTreePresenter.getDisplay());
    expectLastCall().once();
    displayMock.setDetailsDisplay(folderDetailsPresenter.getDisplay());
    expectLastCall().once();

    folderDetailsPresenter.getDisplay().setSelectionEnabled(true);
    expectLastCall().atLeastOnce();
    folderDetailsPresenter.getDisplay().addFileSelectionHandler((FileSelectionHandler) EasyMock.anyObject());
    expectLastCall().once();
  }

  //
  // Test Methods
  //

  @Test
  public void testConstructor() {
    // Setup
    replay(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());

    // Exercise
    new FileSelectorPresenter(displayMock, eventBusMock, fileSystemTreePresenter, folderDetailsPresenter);

    // Verify
    verify(eventBusMock, displayMock, folderDetailsPresenter.getDisplay(), folderDetailsPresenter.getDisplay());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testOnBind() {
    // Setup
    expect(eventBusMock.addHandler((Type<FileSelectionRequiredEvent.Handler>) anyObject(), (FileSelectionRequiredEvent.Handler) anyObject())).andReturn(null).once();
    expect(eventBusMock.addHandler((Type<FileSystemTreeFolderSelectionChangeEvent.Handler>) anyObject(), (FileSystemTreeFolderSelectionChangeEvent.Handler) anyObject())).andReturn(null).once();
    expect(displayMock.addSelectButtonHandler((ClickHandler) anyObject())).andReturn(null).once();
    expect(displayMock.addCreateFolderButtonHandler((ClickHandler) anyObject())).andReturn(null).once();

    replay(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());

    // Exercise
    FileSelectorPresenter sut = new FileSelectorPresenter(displayMock, eventBusMock, fileSystemTreePresenter, folderDetailsPresenter);
    sut.onBind();

    // Verify
    assertEquals(1, fileSystemTreePresenter.getBindCount());
    assertEquals(1, folderDetailsPresenter.getBindCount());
    verify(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());
  }

  @Test
  public void testRevealDisplay() {
    // Setup
    folderDetailsPresenter.getDisplay().clearSelection();
    expectLastCall().once();

    displayMock.clearNewFolderName();
    expectLastCall().once();

    displayMock.setNewFilePanelVisible(true);
    expectLastCall().once();

    displayMock.setNewFolderPanelVisible(true);
    expectLastCall().once();

    displayMock.showDialog();
    expectLastCall().once();

    replay(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());

    // Exercise
    FileSelectorPresenter sut = new FileSelectorPresenter(displayMock, eventBusMock, fileSystemTreePresenter, folderDetailsPresenter);
    sut.revealDisplay();

    // Verify
    verify(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());
  }

  @Test
  public void testRefreshDisplay() {
    // Setup
    replay(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());

    // Exercise
    FileSelectorPresenter sut = new FileSelectorPresenter(displayMock, eventBusMock, fileSystemTreePresenter, folderDetailsPresenter);
    sut.refreshDisplay();

    // Verify
    assertEquals(1, fileSystemTreePresenter.getRefreshCount());
    verify(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());
  }

  @Test
  public void testAllowsFileCreation_ReturnsTrueWhenSelectingFile() {
    // Setup
    replay(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());

    FileSelectorPresenter sut = new FileSelectorPresenter(displayMock, eventBusMock, fileSystemTreePresenter, folderDetailsPresenter);
    sut.setFileSelectionType(FileSelectionType.FILE);

    // Exercise
    boolean allowsFileCreation = sut.allowsFileCreation();

    // Verify
    assertEquals(true, allowsFileCreation);
  }

  @Test
  public void testAllowsFileCreation_ReturnsFalseWhenSelectingExistingFile() {
    // Setup
    replay(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());

    FileSelectorPresenter sut = new FileSelectorPresenter(displayMock, eventBusMock, fileSystemTreePresenter, folderDetailsPresenter);
    sut.setFileSelectionType(FileSelectionType.EXISTING_FILE);

    // Exercise
    boolean allowsFileCreation = sut.allowsFileCreation();

    // Verify
    assertEquals(false, allowsFileCreation);
  }

  @Test
  public void testAllowsFileCreation_ReturnsFalseWhenSelectingFolder() {
    // Setup
    replay(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());

    FileSelectorPresenter sut = new FileSelectorPresenter(displayMock, eventBusMock, fileSystemTreePresenter, folderDetailsPresenter);
    sut.setFileSelectionType(FileSelectionType.FOLDER);

    // Exercise
    boolean allowsFileCreation = sut.allowsFileCreation();

    // Verify
    assertEquals(false, allowsFileCreation);
  }

  @Test
  public void testAllowsFileCreation_ReturnsFalseWhenSelectingExistingFolder() {
    // Setup
    replay(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());

    FileSelectorPresenter sut = new FileSelectorPresenter(displayMock, eventBusMock, fileSystemTreePresenter, folderDetailsPresenter);
    sut.setFileSelectionType(FileSelectionType.EXISTING_FOLDER);

    // Exercise
    boolean allowsFileCreation = sut.allowsFileCreation();

    // Verify
    assertEquals(false, allowsFileCreation);
  }

  @Test
  public void testAllowsFolderCreation_ReturnsFalseWhenSelectingFile() {
    // Setup
    replay(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());

    FileSelectorPresenter sut = new FileSelectorPresenter(displayMock, eventBusMock, fileSystemTreePresenter, folderDetailsPresenter);
    sut.setFileSelectionType(FileSelectionType.FILE);

    // Exercise
    boolean allowsFolderCreation = sut.allowsFolderCreation();

    // Verify
    assertEquals(true, allowsFolderCreation);
  }

  @Test
  public void testAllowsFolderCreation_ReturnsFalseWhenSelectingExistingFile() {
    // Setup
    replay(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());

    FileSelectorPresenter sut = new FileSelectorPresenter(displayMock, eventBusMock, fileSystemTreePresenter, folderDetailsPresenter);
    sut.setFileSelectionType(FileSelectionType.EXISTING_FILE);

    // Exercise
    boolean allowsFolderCreation = sut.allowsFolderCreation();

    // Verify
    assertEquals(false, allowsFolderCreation);
  }

  @Test
  public void testAllowsFolderCreation_ReturnsTrueWhenSelectingFolder() {
    // Setup
    replay(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());

    FileSelectorPresenter sut = new FileSelectorPresenter(displayMock, eventBusMock, fileSystemTreePresenter, folderDetailsPresenter);
    sut.setFileSelectionType(FileSelectionType.FOLDER);

    // Exercise
    boolean allowsFolderCreation = sut.allowsFolderCreation();

    // Verify
    assertEquals(true, allowsFolderCreation);
  }

  @Test
  public void testAllowsFolderCreation_ReturnsFalseWhenSelectingExistingFolder() {
    // Setup
    replay(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());

    FileSelectorPresenter sut = new FileSelectorPresenter(displayMock, eventBusMock, fileSystemTreePresenter, folderDetailsPresenter);
    sut.setFileSelectionType(FileSelectionType.EXISTING_FOLDER);

    // Exercise
    boolean allowsFolderCreation = sut.allowsFolderCreation();

    // Verify
    assertEquals(false, allowsFolderCreation);
  }

  @Test
  public void testGetPlace_ReturnsNull() {
    // Setup
    replay(eventBusMock, displayMock, folderDetailsPresenter.getDisplay());

    // Exercise
    FileSelectorPresenter sut = new FileSelectorPresenter(displayMock, eventBusMock, fileSystemTreePresenter, folderDetailsPresenter);
    Object place = sut.getPlace();

    // Verify
    assertEquals(null, place);
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

  //
  // Inner Classes
  //

  private static class FileSystemTreePresenterSpy extends FileSystemTreePresenter {

    private int bindCount;

    private int refreshCount;

    public FileSystemTreePresenterSpy(FileSystemTreePresenter.Display display, EventBus eventBus) {
      super(display, eventBus);
    }

    protected void onBind() {
      bindCount++;
    }

    public void refreshDisplay() {
      refreshCount++;
    }

    public int getBindCount() {
      return bindCount;
    }

    public int getRefreshCount() {
      return refreshCount;
    }
  }

  private static class FolderDetailsPresenterSpy extends FolderDetailsPresenter {

    private int bindCount;

    public FolderDetailsPresenterSpy(FolderDetailsPresenter.Display display, EventBus eventBus) {
      super(display, eventBus);
    }

    protected void onBind() {
      bindCount++;
    }

    public int getBindCount() {
      return bindCount;
    }
  }
}
