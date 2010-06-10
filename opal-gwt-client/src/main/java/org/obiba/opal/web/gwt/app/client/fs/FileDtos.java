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

import org.obiba.opal.web.model.client.FileDto;

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

}
