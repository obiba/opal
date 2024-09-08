/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.security;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 *
 */
public class TablePermissionConverterTest extends OpalPermissionConverterTest<TablePermissionConverter.Permission> {

  @Test
  public void testArgs() {
    String[] args = DomainPermissionConverter.args("/datasource/patate/table/pwel", "/datasource/(.+)/table/(.+)");
    assertThat(args).isNotNull();
    assertThat(args.length).isEqualTo(2);
    assertThat(args[0]).isEqualTo("patate");
    assertThat(args[1]).isEqualTo("pwel");
  }

  @Test
  public void testTableAll() {
    testConversion("/datasource/patate/table/pwel", TablePermissionConverter.Permission.TABLE_ALL,
        "rest:/datasource/patate/table/pwel:*:GET/*",
        "rest:/datasource/patate/_sql:POST:GET",
        "rest:/datasources/_sql:POST",
        "rest:/project/patate/commands/_export:POST:GET",
        "rest:/project/patate/commands/_copy:POST:GET",
        "rest:/project/patate/commands/_refresh:POST:GET",
        "rest:/project/patate:GET:GET",
        "rest:/project/patate/resources:GET:GET",
        "rest:/project/patate/state:GET:GET",
        "rest:/project/patate/summary:GET:GET",
        "rest:/project/patate/permissions/table/pwel:*:GET/*",
        "rest:/files/projects/patate:GET:GET/*",
        "rest:/files/projects/patate:POST:GET/*",
        "rest:/files/projects/patate:PUT:GET/*",
        "rest:/system/subject-profiles/_search:GET");
  }

  @Test
  public void testTableAllOnView() {
    testConversion("/datasource/patate/view/pwel", TablePermissionConverter.Permission.TABLE_ALL,
        "rest:/datasource/patate/table/pwel:*:GET/*",
        "rest:/datasource/patate/_sql:POST:GET",
        "rest:/datasources/_sql:POST",
        "rest:/project/patate/commands/_export:POST:GET",
        "rest:/project/patate/commands/_copy:POST:GET",
        "rest:/project/patate/commands/_refresh:POST:GET",
        "rest:/project/patate:GET:GET",
        "rest:/project/patate/resources:GET:GET",
        "rest:/project/patate/state:GET:GET",
        "rest:/project/patate/summary:GET:GET",
        "rest:/project/patate/permissions/table/pwel:*:GET/*",
        "rest:/files/projects/patate:GET:GET/*",
        "rest:/files/projects/patate:POST:GET/*",
        "rest:/files/projects/patate:PUT:GET/*",
        "rest:/system/subject-profiles/_search:GET",
        "rest:/datasource/patate/view/pwel:*:GET/*");
  }

  @Test
  public void testTableEdit() {
    testConversion("/datasource/patate/table/pwel", TablePermissionConverter.Permission.TABLE_EDIT,
        "rest:/datasource/patate/table/pwel:PUT:GET",
        "rest:/datasource/patate/table/pwel:DELETE",
        "rest:/datasource/patate/table/pwel/variables:POST:GET/*",
        "rest:/datasource/patate/table/pwel/variables:DELETE:GET",
        "rest:/datasource/patate/table/pwel/index:GET:GET",
        "rest:/datasource/patate/_sql:POST:GET",
        "rest:/datasources/_sql:POST",
        "rest:/datasource/patate/table/pwel/index/schedule:GET:GET",
        "rest:/files/projects/patate:GET:GET/*",
        "rest:/files/projects/patate:POST:GET/*",
        "rest:/files/projects/patate:PUT:GET/*",
        "rest:/datasource/patate/table/pwel:GET:GET",
        "rest:/datasource/patate/table/pwel/index:GET",
        "rest:/datasource/patate/table/pwel/index/_schema:GET",
        "rest:/datasource/patate/table/pwel/variable:GET:GET/GET",
        "rest:/datasource/patate/table/pwel/variables:GET:GET/GET",
        "rest:/datasource/patate/table/pwel/facet:GET:GET/GET",
        "rest:/datasource/patate/table/pwel/facets/_search:POST:GET",
        "rest:/datasource/patate/table/pwel/variable/_transient/summary:POST",
        "rest:/project/patate:GET:GET",
        "rest:/project/patate/resources:GET:GET",
        "rest:/project/patate/state:GET:GET",
        "rest:/project/patate/summary:GET:GET");
  }

  @Test
  public void testTableValuesEdit() {
    testConversion("/datasource/patate/table/pwel", TablePermissionConverter.Permission.TABLE_VALUES_EDIT,
        "rest:/datasource/patate/table/pwel/valueSet:GET:GET/GET",
        "rest:/datasource/patate/table/pwel/entities:GET",
        "rest:/datasource/patate/table/pwel/index:GET:GET/GET",
        "rest:/datasource/patate/table/pwel/index/_search:GET",
        "rest:/datasource/patate/table/pwel/index/_search:POST",
        "rest:/datasource/patate/table/pwel/index/_schema:GET",
        "rest:/datasource/patate/_sql:POST:GET",
        "rest:/datasources/_sql:POST",
        "rest:/project/patate/commands/_analyse:POST:GET",
        "rest:/project/patate/commands/_export:POST:GET",
        "rest:/project/patate/commands/_copy:POST:GET",
        "rest:/project/patate/commands/_refresh:POST:GET",
        "rest:/project/patate:GET:GET",
        "rest:/project/patate/resources:GET:GET",
        "rest:/project/patate/state:GET:GET",
        "rest:/project/patate/summary:GET:GET",
        "rest:/project/patate/analyses:GET:GET",
        "rest:/project/patate/table/pwel/analyses:GET:GET",
        "rest:/project/patate/table/pwel/analyses/_export:GET:GET",
        "rest:/project/patate/table/pwel/analysis:GET:GET/GET",
        "rest:/datasource/patate/table/pwel:GET:GET",
        "rest:/datasource/patate/table/pwel/index:GET",
        "rest:/datasource/patate/table/pwel/index/_schema:GET",
        "rest:/datasource/patate/table/pwel/variable:GET:GET/GET",
        "rest:/datasource/patate/table/pwel/variables:GET:GET/GET",
        "rest:/datasource/patate/table/pwel/facet:GET:GET/GET",
        "rest:/datasource/patate/table/pwel/facets/_search:POST:GET",
        "rest:/datasource/patate/table/pwel/variable/_transient/summary:POST",
        "rest:/project/patate:GET:GET",
        "rest:/project/patate/resources:GET:GET",
        "rest:/project/patate/state:GET:GET",
        "rest:/project/patate/summary:GET:GET",
        "rest:/datasource/patate/table/pwel:PUT:GET",
        "rest:/datasource/patate/table/pwel:DELETE",
        "rest:/datasource/patate/table/pwel/variables:POST:GET/*",
        "rest:/datasource/patate/table/pwel/variables:DELETE:GET",
        "rest:/datasource/patate/table/pwel/index:GET:GET",
        "rest:/datasource/patate/_sql:POST:GET",
        "rest:/datasources/_sql:POST",
        "rest:/datasource/patate/table/pwel/index/schedule:GET:GET",
        "rest:/files/projects/patate:GET:GET/*",
        "rest:/files/projects/patate:POST:GET/*",
        "rest:/files/projects/patate:PUT:GET/*",
        "rest:/datasource/patate/table/pwel:GET:GET",
        "rest:/datasource/patate/table/pwel/index:GET",
        "rest:/datasource/patate/table/pwel/index/_schema:GET",
        "rest:/datasource/patate/table/pwel/variable:GET:GET/GET",
        "rest:/datasource/patate/table/pwel/variables:GET:GET/GET",
        "rest:/datasource/patate/table/pwel/facet:GET:GET/GET",
        "rest:/datasource/patate/table/pwel/facets/_search:POST:GET",
        "rest:/datasource/patate/table/pwel/variable/_transient/summary:POST",
        "rest:/project/patate:GET:GET",
        "rest:/project/patate/resources:GET:GET",
        "rest:/project/patate/state:GET:GET",
        "rest:/project/patate/summary:GET:GET");
  }

  @Test
  public void testTableReadOnView() {
    testConversion("/datasource/patate/view/pwel", TablePermissionConverter.Permission.TABLE_READ,
        "rest:/datasource/patate/table/pwel:GET:GET",
        "rest:/datasource/patate/table/pwel/index:GET",
        "rest:/datasource/patate/table/pwel/index/_schema:GET",
        "rest:/datasource/patate/table/pwel/variable:GET:GET/GET",
        "rest:/datasource/patate/table/pwel/variables:GET:GET/GET",
        "rest:/datasource/patate/table/pwel/facet:GET:GET/GET",
        "rest:/datasource/patate/table/pwel/facets/_search:POST:GET",
        "rest:/datasource/patate/table/pwel/variable/_transient/summary:POST",
        "rest:/project/patate:GET:GET",
        "rest:/project/patate/resources:GET:GET",
        "rest:/project/patate/state:GET:GET",
        "rest:/project/patate/summary:GET:GET",
        "rest:/datasource/patate/view/pwel/xml:GET:GET");
  }

  @Test
  public void testTableValuesOnView() {
    testConversion("/datasource/patate/view/pwel", TablePermissionConverter.Permission.TABLE_VALUES,
        "rest:/datasource/patate/table/pwel/valueSet:GET:GET/GET",
        "rest:/datasource/patate/table/pwel/entities:GET",
        "rest:/datasource/patate/table/pwel/index:GET:GET/GET",
        "rest:/datasource/patate/table/pwel/index/_search:GET",
        "rest:/datasource/patate/table/pwel/index/_search:POST",
        "rest:/datasource/patate/table/pwel/index/_schema:GET",
        "rest:/datasource/patate/_sql:POST:GET",
        "rest:/datasources/_sql:POST",
        "rest:/project/patate/commands/_analyse:POST:GET",
        "rest:/project/patate/commands/_export:POST:GET",
        "rest:/project/patate/commands/_copy:POST:GET",
        "rest:/project/patate/commands/_refresh:POST:GET",
        "rest:/project/patate:GET:GET",
        "rest:/project/patate/resources:GET:GET",
        "rest:/project/patate/state:GET:GET",
        "rest:/project/patate/summary:GET:GET",
        "rest:/project/patate/analyses:GET:GET",
        "rest:/project/patate/table/pwel/analyses:GET:GET",
        "rest:/project/patate/table/pwel/analyses/_export:GET:GET",
        "rest:/project/patate/table/pwel/analysis:GET:GET/GET",
        "rest:/datasource/patate/table/pwel:GET:GET",
        "rest:/datasource/patate/table/pwel/index:GET",
        "rest:/datasource/patate/table/pwel/index/_schema:GET",
        "rest:/datasource/patate/table/pwel/variable:GET:GET/GET",
        "rest:/datasource/patate/table/pwel/variables:GET:GET/GET",
        "rest:/datasource/patate/table/pwel/facet:GET:GET/GET",
        "rest:/datasource/patate/table/pwel/facets/_search:POST:GET",
        "rest:/datasource/patate/table/pwel/variable/_transient/summary:POST",
        "rest:/project/patate:GET:GET",
        "rest:/project/patate/resources:GET:GET",
        "rest:/project/patate/state:GET:GET",
        "rest:/project/patate/summary:GET:GET",
        "rest:/datasource/patate/view/pwel/xml:GET:GET");
  }

  @Override
  protected SubjectPermissionConverter newConverter() {
    return new TablePermissionConverter();
  }

}
