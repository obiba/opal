/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.event;

import org.obiba.opal.web.model.client.FileDto;
import org.obiba.opal.web.model.client.FileDto.FileType;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 *
 */
public class FolderCreationEvent extends GwtEvent<FolderCreationEvent.Handler> {
  //
  // Static Variables
  //

  private static Type<Handler> TYPE;

  //
  // Instance Variables
  //

  private FileDto folder;

  //
  // Constructors
  //

  public FolderCreationEvent(String folderPath) {
    folder = FileDto.create();
    folder.setType(FileType.FOLDER);
    folder.setPath(folderPath);

    int lastPathSeparatorIndex = folderPath.lastIndexOf('/');
    folder.setName(lastPathSeparatorIndex != -1 ? folderPath.substring(lastPathSeparatorIndex + 1) : folderPath);
  }

  //
  // GwtEvent Methods
  //

  @Override
  protected void dispatch(Handler handler) {
    handler.onFolderCreation(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  //
  // Methods
  //

  public static Type<Handler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<Handler>());
  }

  public FileDto getFolder() {
    return folder;
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Handler extends EventHandler {

    public void onFolderCreation(FolderCreationEvent event);
  }
}
