/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.magma.amber;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.base.Strings;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.datasource.csv.CsvDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The tables of an Amber zip export, in the Opal CSV layout: <code>&lt;table&gt;/variables.csv</code>,
 * <code>&lt;table&gt;/data.csv</code> and the collected files as binary values in <code>&lt;table&gt;/files/</code>.
 * The export is extracted in a temporary directory, deleted on dispose as the binary values are loaded lazily.
 */
public class AmberDatasource extends CsvDatasource {

  private static final Logger log = LoggerFactory.getLogger(AmberDatasource.class);

  private static final String DEFAULT_ENTITY_TYPE = "Participant";

  private final File directory;

  public AmberDatasource(String name, InputStream zip, String entityType) throws IOException {
    super(name);
    directory = Files.createTempDirectory("amber-").toFile();
    try {
      unzip(zip, directory.toPath());
      File[] tables = directory.listFiles(File::isDirectory);
      if (tables != null) {
        for (File table : tables) {
          addValueTable(table, Strings.isNullOrEmpty(entityType) ? readEntityType(table) : entityType);
        }
      }
    } catch (IOException | RuntimeException e) {
      deleteDirectory();
      throw e;
    }
  }

  File getDirectory() {
    return directory;
  }

  @Override
  protected void onDispose() {
    super.onDispose();
    deleteDirectory();
  }

  private static void unzip(InputStream zip, Path target) throws IOException {
    try (ZipInputStream zis = new ZipInputStream(zip, StandardCharsets.UTF_8)) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        Path path = target.resolve(entry.getName()).normalize();
        if (!path.startsWith(target)) throw new MagmaRuntimeException("Invalid entry in the Amber export: " + entry.getName());
        if (entry.isDirectory()) {
          Files.createDirectories(path);
        } else {
          Files.createDirectories(path.getParent());
          Files.copy(zis, path);
        }
      }
    }
  }

  /**
   * The entity type of the table variables, e.g. CaseReport when a case report form allows multiple records.
   */
  private static String readEntityType(File table) throws IOException {
    File variables = new File(table, VARIABLES_FILE);
    if (!variables.exists()) return DEFAULT_ENTITY_TYPE;
    try (CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(variables), StandardCharsets.UTF_8))) {
      List<String> header = Arrays.asList(reader.readNext());
      String[] first = reader.readNext();
      int idx = header.indexOf("entityType");
      return first == null || idx < 0 || Strings.isNullOrEmpty(first[idx]) ? DEFAULT_ENTITY_TYPE : first[idx];
    }
  }

  private void deleteDirectory() {
    try (Stream<Path> paths = Files.walk(directory.toPath())) {
      paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    } catch (IOException e) {
      log.warn("Failed to delete the Amber export directory: {}", directory, e);
    }
  }
}
