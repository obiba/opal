/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.magma;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.opal.r.datasource.RAssignDatasourceFactory;
import org.obiba.opal.spi.r.RRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Base implementation of Magma R converters using file dumps to be transferred to R and read back by R.
 */
class ValueTableTibbleRConverter extends AbstractMagmaRConverter {

  private static final Logger log = LoggerFactory.getLogger(ValueTableTibbleRConverter.class);

  private static final String R_CACHE_DIR = System.getenv().get("OPAL_HOME") + File.separatorChar + "work" + File.separatorChar + "R" + File.separatorChar + "cache";

  ValueTableTibbleRConverter(MagmaAssignROperation magmaAssignROperation) {
    super(magmaAssignROperation);
  }

  @Override
  public void doAssign(String symbol, String path) {
    ValueTable table;
    if (magmaAssignROperation.hasValueTable()) table = magmaAssignROperation.getValueTable();
    else table = resolvePath(path);
    if (table == null) throw new IllegalStateException("Table must not be null");

    Stopwatch stopwatch = Stopwatch.createStarted();

    File tableCache = getTableRCache(table);
    boolean assigned = false;
    if (tableCache.exists()) {
      log.info("Assign table '{}' from R cache: {}", table.getName(), tableCache.getName());
      try (InputStream is = new FileInputStream(tableCache)) {
        magmaAssignROperation.doWriteFile(tableCache.getName(), is);
        magmaAssignROperation.doEval(String.format("is.null(base::assign('%s', readRDS('%s')))", getSymbol(), tableCache.getName()));
        magmaAssignROperation.doEval(String.format("base::unlink('%s')", tableCache.getName()));
        assigned = true;
      } catch (IOException e) {
         log.warn("Failed at reinstating table R cache: {}", tableCache.getName(), e);
      }
    }

    if (!assigned) {
      log.info("Assign table '{}' from datasource: {}", table.getName(), table.getDatasource().getName());
      RAssignDatasourceFactory factory = new RAssignDatasourceFactory(table.getDatasource().getName() + "-r", getSymbol(), magmaAssignROperation.getRConnection());
      factory.setIdColumnName(magmaAssignROperation.getIdColumnName());
      factory.setWithMissings(magmaAssignROperation.withMissings());
      Datasource ds = factory.create();
      Initialisables.initialise(ds);

      DatasourceCopier.Builder copier = magmaAssignROperation.getDataExportService().newCopier(ds);
      try {
        magmaAssignROperation.getDataExportService().exportTablesToDatasource(null,
            Sets.newHashSet(table), ds, copier, false, null);
        magmaAssignROperation.doEval(String.format("if (!exists('%s')) assign('%s', NULL)", getSymbol(), getSymbol()));
      } catch (InterruptedException e) {
        log.error("Interrupted while assigning table to R", e);
      } finally {
        Disposables.silentlyDispose(ds);
      }

      try {
        magmaAssignROperation.doEval(String.format("saveRDS(`%s`, '%s')", getSymbol(), tableCache.getName()));
        magmaAssignROperation.doReadFile(tableCache.getName(), tableCache);
        magmaAssignROperation.doEval(String.format("base::unlink('%s')", tableCache.getName()));
      } catch (Exception e) {
        log.warn("Table R cache failure", e);
        tableCache.delete();
      }
    }
    log.info("R assignment succeed in {}", stopwatch.stop());
  }

  private File getTableRCache(ValueTable table) {
    String cacheKey = table.getDatasource().getName() + "-" + table.getName() + "-" +
        magmaAssignROperation.getIdColumnName() + "-" + magmaAssignROperation.withMissings() +  "-" +
        ((Date)table.getTimestamps().getLastUpdate().getValue()).getTime();
    File tableCache = new File(R_CACHE_DIR, cacheKey + ".rds");
    return tableCache;
  }

}
