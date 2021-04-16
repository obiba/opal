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

import com.google.common.base.Strings;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.MagmaEngineReferenceResolver;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.magma.views.View;
import org.obiba.opal.core.magma.IdentifiersMappingView;
import org.obiba.opal.spi.r.datasource.magma.MagmaRRuntimeException;

/**
 * Base implementation of Magma vector providers.
 */
abstract class AbstractMagmaRConverter implements MagmaRConverter {

  MagmaAssignROperation magmaAssignROperation;

  AbstractMagmaRConverter(MagmaAssignROperation magmaAssignROperation) {
    this.magmaAssignROperation = magmaAssignROperation;
  }

  protected String getSymbol() {
    return magmaAssignROperation.getSymbol();
  }

  protected boolean withIdColumn() {
    return magmaAssignROperation.withIdColumn();
  }

  protected String getIdColumnName() {
    return magmaAssignROperation.getIdColumnName();
  }

  ValueTable applyIdentifiersMapping(ValueTable table) {
    String idMapping = magmaAssignROperation.getIdentifiersMapping();
    // If the table contains an entity that requires identifiers separation, create a "identifers view" of the table (replace
    // public (system) identifiers with private identifiers).
    if (!Strings.isNullOrEmpty(idMapping) &&
        magmaAssignROperation.getIdentifiersTableService().hasIdentifiersMapping(table.getEntityType(), idMapping)) {
      // Make a view that converts opal identifiers to unit identifiers
      return new IdentifiersMappingView(idMapping, IdentifiersMappingView.Policy.UNIT_IDENTIFIERS_ARE_PUBLIC, table,
          magmaAssignROperation.getIdentifiersTableService());
    }
    return table;
  }

  ValueTable applyVariableFilter(ValueTable table, String varName) {
    ValueTable filteredTable = table;
    if (!Strings.isNullOrEmpty(varName)) {
      View view = new View(table.getName(), table);
      view.setSelectClause(variable -> variable.getName().equals(varName));
      Initialisables.initialise(view);
      filteredTable = view;
    } else if (magmaAssignROperation.hasVariableFilter()) {
      View view = new View(table.getName(), table);
      view.setSelectClause(new JavascriptClause(magmaAssignROperation.getVariableFilter()));
      Initialisables.initialise(view);
      filteredTable = view;
    }
    return filteredTable;
  }

  ValueTable resolvePath(String path) {
    MagmaEngineReferenceResolver resolver = path.contains(":") ?
        MagmaEngineVariableResolver.valueOf(path) : MagmaEngineTableResolver.valueOf(path);

    if (resolver.getDatasourceName() == null)
      throw new MagmaRRuntimeException("Datasource is not defined in path: " + path);
    Datasource ds = MagmaEngine.get().getDatasource(resolver.getDatasourceName());
    ValueTable table = applyIdentifiersMapping(ds.getValueTable(resolver.getTableName()));
    return applyVariableFilter(table, resolver.getVariableName());
  }
}
