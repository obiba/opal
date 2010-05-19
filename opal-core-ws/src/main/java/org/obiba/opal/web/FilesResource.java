/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.web.model.Opal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
@Path("/files")
public class FilesResource {

  @Autowired
  private OpalRuntime opalRuntime;

  private static final Logger log = LoggerFactory.getLogger(FilesResource.class);

  private List<Opal.FileDto> files = Lists.newArrayList();

  @GET
  public List<Opal.FileDto> getFiles() throws FileSystemException {
    addFiles(opalRuntime.getFileSystem().getRoot());
    return files;
  }

  private void addFiles(FileObject parent) throws FileSystemException {
    if(parent.getType() == FileType.FOLDER) {
      FileObject[] children = parent.getChildren();
      for(int i = 0; i < children.length; i++) {
        addFile(children[i]);
        addFiles(children[i]);
      }
    } else {
      addFile(parent);
    }
  }

  private void addFile(FileObject file) {
    Opal.FileDto.Builder fileBuilder = Opal.FileDto.newBuilder().setName(file.getName().getPath());
    files.add(fileBuilder.build());
  }

}
