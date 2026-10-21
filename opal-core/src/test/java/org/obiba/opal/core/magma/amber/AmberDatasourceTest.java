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

import au.com.bytecode.opencsv.CSVWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.*;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.DecimalType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.*;

public class AmberDatasourceTest {

  @Before
  public void before() {
    new MagmaEngine();
  }

  @After
  public void after() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void test_amber_export() throws IOException {
    AmberDatasource ds = new AmberDatasource("amber", new ByteArrayInputStream(amberZip()), null);
    Initialisables.initialise(ds);

    ValueTable crf = ds.getValueTable("CRF-1");
    assertEquals("Participant", crf.getEntityType());
    VariableEntity p1 = crf.getVariableEntities().iterator().next();
    assertEquals("P1", p1.getIdentifier());
    ValueSet vs = crf.getValueSet(p1);

    Variable doc = crf.getVariable("doc");
    assertEquals(BinaryType.get(), doc.getValueType());
    assertEquals("hello", new String((byte[]) crf.getValue(doc, vs).getValue(), StandardCharsets.UTF_8));

    Variable names = crf.getVariable("CONTACTS.name");
    assertEquals("CONTACTS", names.getOccurrenceGroup());
    assertEquals("[Al \"Bo\", Cy]", crf.getValue(names, vs).asSequence().getValues().stream().map(Value::toString).toList().toString());
    assertEquals(DecimalType.get(), crf.getVariable("weight").getValueType());

    // a case report form with multiple records per participant
    assertEquals("CaseReport", ds.getValueTable("MULTI-1").getEntityType());

    Disposables.dispose(ds);
    assertFalse(ds.getDirectory().exists());
  }

  @Test(expected = MagmaRuntimeException.class)
  public void test_zip_slip() throws IOException {
    new AmberDatasource("amber", new ByteArrayInputStream(zip("../evil.txt", "x")), null);
  }

  /**
   * As written by Amber: the lists are Opal value sequences, the files are relative to data.csv.
   */
  private byte[] amberZip() throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(out)) {
      add(zos, "CRF-1/variables.csv", csv(
          new String[] { "name", "entityType", "valueType", "mimeType", "repeatable", "occurrenceGroup", "index" },
          new String[] { "doc", "Participant", "binary", "application/octet-stream", "false", "", "0" },
          new String[] { "CONTACTS.name", "Participant", "text", "", "true", "CONTACTS", "1" },
          new String[] { "weight", "Participant", "decimal", "", "false", "", "2" }));
      add(zos, "CRF-1/data.csv", csv(
          new String[] { "_id", "doc", "CONTACTS.name", "weight" },
          new String[] { "P1", "files/f1/doc.txt", "\"Al \"\"Bo\"\"\",\"Cy\"", "70.5" }));
      add(zos, "CRF-1/files/f1/doc.txt", "hello");
      add(zos, "MULTI-1/variables.csv", csv(
          new String[] { "name", "entityType", "valueType", "referencedEntityType", "repeatable", "index" },
          new String[] { "id", "CaseReport", "text", "Participant", "true", "0" }));
      add(zos, "MULTI-1/data.csv", csv(new String[] { "_id", "id" }, new String[] { "CR1", "P1" }));
    }
    return out.toByteArray();
  }

  private byte[] zip(String name, String content) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(out)) {
      add(zos, name, content);
    }
    return out.toByteArray();
  }

  private static void add(ZipOutputStream zos, String name, String content) throws IOException {
    zos.putNextEntry(new ZipEntry(name));
    zos.write(content.getBytes(StandardCharsets.UTF_8));
    zos.closeEntry();
  }

  private static String csv(String[]... rows) throws IOException {
    StringWriter writer = new StringWriter();
    try (CSVWriter csv = new CSVWriter(writer)) {
      for (String[] row : rows) csv.writeNext(row);
    }
    return writer.toString();
  }
}
