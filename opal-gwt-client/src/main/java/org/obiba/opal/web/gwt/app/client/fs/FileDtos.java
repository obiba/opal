/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.fs;

import org.obiba.opal.web.model.client.opal.FileDto;
import org.obiba.opal.web.model.client.opal.FileDto.FileType;

/**
 *
 */
public class FileDtos {

  public static boolean isFolder(FileDto dto) {
    if(dto == null) throw new IllegalArgumentException("dto cannot be null");
    return FileDto.FileType.FOLDER.isFileType(dto.getType());
  }

  public static boolean isFile(FileDto dto) {
    if(dto == null) throw new IllegalArgumentException("dto cannot be null");
    return FileDto.FileType.FILE.isFileType(dto.getType());
  }

  public static FileDto getParent(FileDto dto) {
    FileDto parentDto = FileDto.create();
    parentDto.setType(FileType.FOLDER);
    parentDto.setPath(getParentFolderPath(dto.getPath()));
    parentDto.setName(getFolderName(parentDto.getPath()));
    return parentDto;
  }

  private static String getParentFolderPath(String childPath) {
    String parentPath = null;

    int lastSeparatorIndex = childPath.lastIndexOf('/');

    if(lastSeparatorIndex != -1) {
      parentPath = lastSeparatorIndex != 0 ? childPath.substring(0, lastSeparatorIndex) : "/";
    }

    return parentPath;
  }

  private static String getFolderName(String folderPath) {
    String folderName = folderPath;

    if(!folderPath.equals("/")) {
      int lastSeparatorIndex = folderPath.lastIndexOf('/');

      if(lastSeparatorIndex != -1) {
        folderName = folderPath.substring(lastSeparatorIndex + 1);
      }
    } else {
      folderName = "root";
    }

    return folderName;
  }
}
