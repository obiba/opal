/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
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

  private ValueTypeMappings valueTypeMappings = new ValueTypeMappings();

  private Iterable<VariableMapping> mappings = ImmutableList.<VariableMapping> of(new Categorical()/* , new Store() */);

  public XContentBuilder map(Variable variable, XContentBuilder builder) {
    try {
      builder.startObject(variable.getName());

      valueTypeMappings.forType(variable.getValueType()).map(builder);

      for(VariableMapping variableMapping : mappings) {
        variableMapping.map(variable, builder);
      }

      return builder.endObject();
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  private class Store implements VariableMapping {

    @Override
    public void map(Variable variable, XContentBuilder builder) throws IOException {
      builder.field("store", "yes");
    }
  }

  /**
   * Used to prevent Lucene analyzers from running on categorical values
   */
  private class Categorical implements VariableMapping {

    @Override
    public void map(Variable variable, XContentBuilder builder) throws IOException {
      if(variable.hasCategories() && variable.getValueType() == TextType.get()) {
        builder.field("index", "not_analyzed");
      }
    }
  }
}
