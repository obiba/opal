/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
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
import org.obiba.magma.Variable;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.opal.r.datasource.RAssignDatasourceFactory;
import org.obiba.opal.r.service.RCacheHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * Base implementation of Magma R converters using file dumps to be transferred to R and read back by R.
 */
class ValueTableTibbleRConverter extends AbstractMagmaRConverter {

  private static final Logger log = LoggerFactory.getLogger(ValueTableTibbleRConverter.class);

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

    String cacheKey = getTableRCacheKey(table);
    String rdsFileName = String.format("%s.rds", cacheKey);
    boolean assigned = false;
    RCacheHelper rCacheHelper = magmaAssignROperation.getRCacheHelper();
    if (rCacheHelper.hasCache(cacheKey)) {
      log.info("Assign table '{}' from R cache: {}", table.getName(), cacheKey);
      try (InputStream is = rCacheHelper.newRDSInputStream(cacheKey)) {
        magmaAssignROperation.doWriteFile(rdsFileName, is);
        magmaAssignROperation.doEval(String.format("is.null(base::assign('%s', readRDS('%s')))", getSymbol(), rdsFileName));
        magmaAssignROperation.doEval(String.format("base::unlink('%s')", rdsFileName));
        assigned = true;
      } catch (IOException e) {
         log.warn("Failed at reinstating table R cache: {}", cacheKey, e);
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

      if (!rCacheHelper.hasCache(cacheKey)) {
        try {
          magmaAssignROperation.doEval(String.format("saveRDS(`%s`, '%s')", getSymbol(), rdsFileName));
          magmaAssignROperation.doReadFile(rdsFileName, rCacheHelper.newRDSOutputStream(cacheKey));
          magmaAssignROperation.doEval(String.format("base::unlink('%s')", rdsFileName));
        } catch (Exception e) {
          log.warn("Table R cache failure", e);
          rCacheHelper.evictCache(cacheKey);
        }
      }
    }
    log.info("R assignment succeed in {}", stopwatch.stop());
  }

  private String getTableRCacheKey(ValueTable table) {
    String parametersKey = magmaAssignROperation.getIdColumnName() + "-" +
        magmaAssignROperation.withMissings() +  "-" +
        magmaAssignROperation.getIdentifiersMapping();
    try {
      parametersKey = parametersKey + "-" + (magmaAssignROperation.hasVariableFilter() ?
            URLEncoder.encode(magmaAssignROperation.getVariableFilter(), StandardCharsets.UTF_8) : "null");
    } catch (Exception e) {
      // ignore
    }
    // some users can see only some of the variables, then cache key should be aware of that
    String varsKey = StreamSupport.stream(table.getVariables().spliterator(), false)
        .map(Variable::getName)
        .collect(Collectors.joining("|"));
    parametersKey = parametersKey + "-" + varsKey;
    String cacheKey = table.getDatasource().getName() + "-" + table.getName() + "-" +
        getCRC32Checksum(parametersKey.getBytes()) + "-" +
        ((Date)table.getTimestamps().getLastUpdate().getValue()).getTime();
    return cacheKey;
  }

  public static long getCRC32Checksum(byte[] bytes) {
    Checksum crc32 = new CRC32();
    crc32.update(bytes, 0, bytes.length);
    return crc32.getValue();
  }

}
