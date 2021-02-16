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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    RAssignDatasourceFactory factory = new RAssignDatasourceFactory(table.getDatasource().getName() + "-r", getSymbol(), magmaAssignROperation.getRConnection());
    factory.setIdColumnName(magmaAssignROperation.getIdColumnName());
    factory.setWithMissings(magmaAssignROperation.withMissings());
    Datasource ds = factory.create();
    Initialisables.initialise(ds);

    DatasourceCopier.Builder copier = magmaAssignROperation.getDataExportService().newCopier(ds);
    try {
      magmaAssignROperation.getDataExportService().exportTablesToDatasource(null,
          Sets.newHashSet(table), ds, copier, false, null);
    } catch (InterruptedException e) {
      log.error("Interrupted while assigning table to R", e);
    } finally {
      Disposables.silentlyDispose(ds);
    }
    log.info("R assignment succeed in {}", stopwatch.stop());
  }

}
