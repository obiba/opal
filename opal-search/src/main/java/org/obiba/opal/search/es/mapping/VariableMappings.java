/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.search.es.mapping;

import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.obiba.magma.Variable;
import org.obiba.magma.type.TextType;

import com.google.common.collect.ImmutableList;

public class VariableMappings {

//  private static final Logger log = LoggerFactory.getLogger(VariableMappings.class);

  private final ValueTypeMappings valueTypeMappings = new ValueTypeMappings();

  private final Iterable<VariableMapping> mappings = ImmutableList
      .<VariableMapping>of(new Categorical()/* , new Store() */);

  public XContentBuilder map(String tableName, Variable variable, XContentBuilder builder) {
    try {
      builder.startObject(tableName + "-" + variable.getName());

      valueTypeMappings.forType(variable.getValueType()).map(builder);

      for(VariableMapping variableMapping : mappings) {
        variableMapping.map(variable, builder);
      }

      return builder.endObject();
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static class Store implements VariableMapping {

    @Override
    public void map(Variable variable, XContentBuilder builder) throws IOException {
      builder.field("store", "yes");
    }
  }

  /**
   * Used to prevent Lucene analyzers from running on categorical values
   */
  private static class Categorical implements VariableMapping {

    @Override
    public void map(Variable variable, XContentBuilder builder) throws IOException {
      if(variable.hasCategories() && variable.getValueType() == TextType.get()) {
        builder.field("index", "not_analyzed");
      }
    }
  }
}
