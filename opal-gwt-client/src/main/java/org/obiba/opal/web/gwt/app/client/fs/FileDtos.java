/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.fs;

import java.util.List;

import org.obiba.opal.web.model.client.opal.FileDto;
import org.obiba.opal.web.model.client.opal.FileDto.FileType;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.http.client.URL;

/**
 *
 */
public class FileDtos {

  private FileDtos() {}

  public static boolean isFolder(FileDto dto) {
    if(dto == null) throw new IllegalArgumentException("dto cannot be null");
    return FileDto.FileType.FOLDER.isFileType(dto.getType());
  }

  public static boolean isFile(FileDto dto) {
    if(dto == null) throw new IllegalArgumentException("dto cannot be null");
    return FileDto.FileType.FILE.isFileType(dto.getType());
  }

  public static String getLink(FileDto file) {
    // Append / if pasting directly under File System (/)
    return "/files" + file.getPath() + ("/".equals(file.getPath()) ? URL.encodePathSegment("/") : "");
  }

  public static FileDto users() {
    return create("home");
  }

  public static FileDto user(String name) {
    return Strings.isNullOrEmpty(name) ? users() : create("home", name);
  }

  public static FileDto projects() {
    return create("projects");
  }

  public static FileDto project(String name) {
    return Strings.isNullOrEmpty(name) ? projects() : create("projects", name);
  }

  public static FileDto units() {
    return create("units");
  }

  public static FileDto unit(String name) {
    return Strings.isNullOrEmpty(name) ? units() : create("units", name);
  }

  public static FileDto reports() {
    return create("reports");
  }

  public static FileDto report(String name) {
    return Strings.isNullOrEmpty(name) ? reports() : create("reports", name);
  }

  public static FileDto create(String... segments) {
    FileDto file = FileDto.create();
    file.setType(FileType.FOLDER);
    if(segments == null || segments.length == 0) {
      file.setName("");
      file.setPath("/");
    } else {
      file.setName(segments[segments.length - 1]);
      String path = "";
      for(String segment : segments) {
        if(!Strings.isNullOrEmpty(segment)) {
          path = path + "/" + segment;
        }
      }
      file.setPath(path);
    }
    return file;
  }

  public static FileDto getParent(FileDto dto) {
    FileDto parentDto = FileDto.create();
    parentDto.setType(FileType.FOLDER);
    parentDto.setPath(getParentFolderPath(dto.getPath()));
    parentDto.setName(getFolderName(parentDto.getPath()));
    parentDto.setReadable(dto.getReadable());
    return parentDto;
  }

  @SuppressWarnings("PMD.NcssMethodCount")
  public static List<FileDto> getParents(FileDto dto) {
    String[] segments = getPathSegments(dto);
    List<FileDto> parents = Lists.newArrayList();
    if(segments.length == 0) return parents;

    FileDto current = null;
    for(int i = 0; i < segments.length - 1; i++) {
      String segment = segments[i];
      FileDto parentDto = FileDto.create();
      parentDto.setType(FileType.FOLDER);
      parentDto.setName(segment);
      parentDto.setReadable(dto.getReadable());
      if(current == null) {
        parentDto.setPath("/");
      } else if(current.getName().isEmpty()) {
        parentDto.setPath("/" + segment);
      } else {
        parentDto.setPath(current.getPath() + "/" + segment);
      }
      parents.add(parentDto);
      current = parentDto;
    }

    return parents;
  }

  public static String[] getPathSegments(FileDto file) {
    return file.getPath().split("/");
  }

  private static String getParentFolderPath(String childPath) {
    String parentPath = null;

    int lastSeparatorIndex = childPath.lastIndexOf('/');

    if(lastSeparatorIndex != -1) {
      parentPath = lastSeparatorIndex == 0 ? "/" : childPath.substring(0, lastSeparatorIndex);
    }

    return parentPath;
  }

  private static String getFolderName(String folderPath) {
    String folderName = folderPath;

    if("/".equals(folderPath)) {
      folderName = "root";
    } else {
      int lastSeparatorIndex = folderPath.lastIndexOf('/');

      if(lastSeparatorIndex != -1) {
        folderName = folderPath.substring(lastSeparatorIndex + 1);
      }
    }

    return folderName;
  }
}
