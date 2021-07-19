/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.sql;

import com.google.common.base.Joiner;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class SQLExtractorTest {

  @Test
  public void testExtractTables() throws SQLParserException {
    doExtractTables("select * from CNSIM1", "CNSIM1");
    doExtractTables("select tab.LAB_GLUC from CNSIM1 as tab", "CNSIM1");
    doExtractTables("select * from CNSIM1 union all select * from CNSIM2", "CNSIM1 | CNSIM2");
    doExtractTables("select count(LAB_HDL) as hdl_count, avg(LAB_HDL) as hdl_avg, GENDER as gender " +
        "from (select * from CNSIM1 union all select * from CNSIM2) " +
        "where LAB_HDL>0 " +
        "group by GENDER", "CNSIM1 | CNSIM2");
    doExtractTables("select 1", "");
  }

  private void doExtractTables(String sql, String expected) throws SQLParserException {
    Set<String> tables = SQLExtractor.extractTables(sql);
    String names = Joiner.on(" | ").join(tables);
    System.out.println(names);
    Assert.assertEquals(expected, names);
  }
}
