/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.fs.event;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter.FileSelection;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 *
 */
public class FileSelectionEvent extends GwtEvent<FileSelectionEvent.Handler> {
  //
  // Static Variables
  //

  private static final Type<Handler> TYPE = new Type<>();

  //
  // Instance Variables
  //

  private final Object source;

  private final FileSelection selectedFile;

  //
  // Constructors
  //

  public FileSelectionEvent(Object source, FileSelection selectedFile) {
    this.source = source;
    this.selectedFile = selectedFile;
  }

  //
  // GwtEvent Methods
  //

  @Override
  protected void dispatch(Handler handler) {
    handler.onFileSelection(this);
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

  public FileSelection getSelectedFile() {
    return selectedFile;
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Handler extends EventHandler {

    void onFileSelection(FileSelectionEvent event);
  }
}
