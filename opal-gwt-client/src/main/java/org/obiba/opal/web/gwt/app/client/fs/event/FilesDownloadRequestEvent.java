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

import org.obiba.opal.web.gwt.app.client.fs.FileDtos;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 *
 */
public class FilesDownloadRequestEvent extends GwtEvent<FilesDownloadRequestEvent.Handler> {

  public interface Handler extends EventHandler {

    void onFilesDownloadRequest(FilesDownloadRequestEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  private final FileDto parent;

  private final List<FileDto> children;

  public FilesDownloadRequestEvent(FileDto parent, List<FileDto> children) {
    this.parent = parent;
    this.children = children;
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  public FileDto getParent() {
    return parent;
  }

  public String getParentLink() {
    return FileDtos.getLink(parent);
  }

  public Iterable<FileDto> getChildren() {
    return children;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onFilesDownloadRequest(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return getType();
  }
}
