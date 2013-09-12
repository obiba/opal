/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.fs.event;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter.FileSelection;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 *
 */
public class FilesCheckedEvent extends GwtEvent<FilesCheckedEvent.Handler> {
  //
  // Static Variables
  //

  private static final Type<Handler> TYPE = new Type<Handler>();

  //
  // Instance Variables
  //

  private final List<FileDto> checkedFiles;

  //
  // Constructors
  //

  public FilesCheckedEvent(List<FileDto> checkedFiles) {
    this.checkedFiles= checkedFiles;
  }

  //
  // GwtEvent Methods
  //

  @Override
  protected void dispatch(Handler handler) {
    handler.onFilesChecked(this);
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

  public List<FileDto> getCheckedFiles() {
    return checkedFiles;
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Handler extends EventHandler {

    void onFilesChecked(FilesCheckedEvent event);
  }
}
