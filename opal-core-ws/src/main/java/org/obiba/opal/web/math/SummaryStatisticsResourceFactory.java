/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.math;

import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.opal.core.domain.VariableNature;
import org.obiba.opal.web.math.support.CategoricalSummaryStatsQuery;

public class SummaryStatisticsResourceFactory {

  public AbstractSummaryStatisticsResource getResource(ValueTable valueTable, VariableValueSource vvs,
      String natureStr) {
    VectorSource vectorSource = vvs.asVectorSource();

    if(vectorSource != null) {
      VariableNature nature = natureStr == null ? VariableNature.getNature(vvs.getVariable()) : VariableNature
          .valueOf(natureStr.toUpperCase());
      switch(nature) {
        case CATEGORICAL:
          return new CategoricalSummaryStatisticsResource(valueTable, vvs.getVariable(), vectorSource);
        case CONTINUOUS:
          return new ContinuousSummaryStatisticsResource(valueTable, vvs.getVariable(), vectorSource);
        case TEMPORAL:
        case UNDETERMINED:
          // fall-through
      }
    }
    return new DefaultSummaryStatisticsResource(valueTable, vvs.getVariable(), vectorSource);
  }

}
