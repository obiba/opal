/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.fs.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter.FileSelectionType;

/**
 *
 */
public class FileSelectionRequestEvent extends GwtEvent<FileSelectionRequestEvent.Handler> {
  //
  // Static Variables
  //

  private static final Type<Handler> TYPE = new Type<Handler>();

  //
  // Instance Variables
  //

  private final Object source;

  private final FileSelectionType fileSelectionType;

  private final String project;

  private final String fileFilter;

  //
  // Constructors
  //

  public FileSelectionRequestEvent(Object source, FileSelectionType fileSelectionType) {
    this(source, fileSelectionType, null);
  }

  public FileSelectionRequestEvent(Object source, FileSelectionType fileSelectionType, String project) {
    this(source, fileSelectionType, project, null);
  }

  public FileSelectionRequestEvent(Object source, FileSelectionType fileSelectionType, String project, String fileFilter) {
    this.source = source;
    this.fileSelectionType = fileSelectionType;
    this.fileFilter = fileFilter;
    this.project = project;
  }

  //
  // GwtEvent Methods
  //

  @Override
  protected void dispatch(Handler handler) {
    handler.onFileSelectionRequired(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return getType();
  }

  //
  // Methods
  //

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  public Object getSource() {
    return source;
  }

  public FileSelectionType getFileSelectionType() {
    return fileSelectionType;
  }

  public String getFileFilter() {
    return fileFilter;
  }

  public String getProject() {
    return project;
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Handler extends EventHandler {

    void onFileSelectionRequired(FileSelectionRequestEvent event);
  }
}
