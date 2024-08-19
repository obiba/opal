/*
 * Copyright (c) 2024 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.search.service.impl;

import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.type.DateTimeType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Date;

public class FileTimestamps implements Timestamps {

  private final File file;

  public FileTimestamps(File file) {
    this.file = file;
  }

  @Override
  public Value getLastUpdate() {
    Path filePath = file.toPath();
    try {
      FileTime fileTime = Files.getLastModifiedTime(filePath, LinkOption.NOFOLLOW_LINKS);
      return DateTimeType.get().valueOf(new Date(fileTime.toMillis()));
    } catch (IOException e) {
      return DateTimeType.get().nullValue();
    }
  }

  @Override
  public Value getCreated() {
    Path filePath = file.toPath();
    try {
      BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
      FileTime creationTime = attrs.creationTime();
      return DateTimeType.get().valueOf(new Date(creationTime.toMillis()));
    } catch (IOException e) {
      return DateTimeType.get().nullValue();
    }
  }
}
