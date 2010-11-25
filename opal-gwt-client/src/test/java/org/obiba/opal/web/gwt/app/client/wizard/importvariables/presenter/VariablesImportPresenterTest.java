/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import net.customware.gwt.presenter.client.EventBus;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;

import com.google.gwt.event.dom.client.ClickEvent;

/**
 * Unit tests for {@link VariablesImportPresenter}.
 */
public class VariablesImportPresenterTest {
  //
  // Instance Variables
  //

  private VariablesImportPresenter.Display displayMock;

  private EventBus eventBusMock;

  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() {
    displayMock = createMock(VariablesImportPresenter.Display.class);
    eventBusMock = createMock(EventBus.class);
  }

  //
  // Test Methods
  //

  @Test
  public void testConstructor() {
    // Setup
    replay(displayMock, eventBusMock);

    // Exercise
    VariablesImportPresenter sut = new VariablesImportPresenter(displayMock, eventBusMock);

    // Verify
    assertEquals(displayMock, sut.getDisplay());
    verify(displayMock, eventBusMock);
  }

  @Test
  public void testRevealDisplay() {
    // Setup
    displayMock.showDialog();
    expectLastCall().once();

    replay(displayMock, eventBusMock);

    // Exercise
    VariablesImportPresenter sut = new VariablesImportPresenter(displayMock, eventBusMock);
    sut.revealDisplay();

    // Verify
    verify(displayMock, eventBusMock);
  }

  @Test
  public void testDownloadExcelTemplateButton_FiresFileDownloadEvent() {
    // Setup
    eventBusMock.fireEvent(eqFileDownloadEvent(new FileDownloadEvent("prefix/ws/templates/opalVariableTemplate.xls")));
    expectLastCall().once();

    replay(displayMock, eventBusMock);

    VariablesImportPresenter presenter = new VariablesImportPresenter(displayMock, eventBusMock);

    // Exercise
    VariablesImportPresenter.DownloadExcelTemplateClickHandler sut = presenter.new DownloadExcelTemplateClickHandler() {

      @Override
      String getUrlPrefix() {
        return "prefix/";
      }
    };
    sut.onClick(new DummyClickEvent());

    // Verify
    verify(eventBusMock, displayMock);
  }

  //
  // Inner Classes
  //

  private static class DummyClickEvent extends ClickEvent {
  }

  private static class FileDownloadEventMatcher implements IArgumentMatcher {

    private FileDownloadEvent expected;

    public FileDownloadEventMatcher(FileDownloadEvent expected) {
      this.expected = expected;
    }

    @Override
    public boolean matches(Object actual) {
      if(actual instanceof FileDownloadEvent) {
        return ((FileDownloadEvent) actual).getUrl().equals(expected.getUrl());
      } else {
        return false;
      }
    }

    @Override
    public void appendTo(StringBuffer buffer) {
      buffer.append("eqFileDownloadEvent(");
      buffer.append(expected.getClass().getName());
      buffer.append(" with url \"");
      buffer.append(expected.getUrl());
      buffer.append("\")");
    }

  }

  private static FileDownloadEvent eqFileDownloadEvent(FileDownloadEvent in) {
    EasyMock.reportMatcher(new FileDownloadEventMatcher(in));
    return null;
  }
}
