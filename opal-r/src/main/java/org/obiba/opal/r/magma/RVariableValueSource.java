/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.magma;

import com.google.common.collect.Lists;
import org.obiba.magma.*;
import org.obiba.magma.type.*;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPGenericVector;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.RList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.SortedSet;

/**
 * The R variable represents the column of a tibble.
 */
class RVariableValueSource extends AbstractVariableValueSource implements VariableValueSource, VectorSource {

  private static final Logger log = LoggerFactory.getLogger(RVariableValueSource.class);

  private RValueTable valueTable;

  private final String colname;

  private final int position;

  private Variable variable;

  RVariableValueSource(RValueTable valueTable, String colname, int position, REXP col, REXP attr) {
    this.valueTable = valueTable;
    this.colname = colname;
    this.position = position;
    initialiseVariable(col, attr);
  }

  private void initialiseVariable(REXP col, REXP attr) {
    ValueType type = TextType.get();
    if (col.isInteger()) type = IntegerType.get();
    else if (col.isNumeric()) type = DecimalType.get();
    else if (col.isLogical()) type = BooleanType.get();
    else if (col.isRaw()) type = BinaryType.get();
    this.variable = VariableBean.Builder.newVariable(colname, type, valueTable.getEntityType())
        .addAttributes(extractAttributes(attr)).addCategories(extractCategories(attr)).build();
  }

  private List<Attribute> extractAttributes(REXP attr) {
    List<Attribute> attributes = Lists.newArrayList();
    if (attr == null || !attr.isList()) return attributes;
    try {
      RList rList = attr.asList();
      if (!rList.isNamed()) return attributes;
      for (String key : rList.keys()) {
        if (key.equals("labels")) continue;
        REXP value = rList.at(key);
        String name = key;
        String namespace = null;
        if (key.contains("::")) {
          String[] nsn = key.split("::");
          name = nsn[1];
          namespace = nsn[0];
        }
        if (value.isString() && value.length() > 0) {
          attributes.add(Attribute.Builder.newAttribute(name).withNamespace(namespace).withValue(value.asStrings()[0]).build());
        } else {
          log.info("Attribute value is a {}", value.getClass().getName());
        }
      }

    } catch (REXPMismatchException e) {
      // ignore
      log.warn("Error while parsing variable attributes: {}", colname, e);
    }
    return attributes;
  }

  private List<Category> extractCategories(REXP attr) {
    List<Category> categories = Lists.newArrayList();
    if (attr == null || !attr.isList()) return categories;
    try {
      RList rList = attr.asList();
      if (!rList.isNamed() || !rList.containsKey("labels")) return categories;
      REXP catNames = rList.at("labels");
      String[] catLabels = null;
      if (catNames.hasAttribute("names")) {
        catLabels = catNames.getAttribute("names").asStrings();
      }
      int i = 0;
      for (String name : catNames.asStrings()) {
        Category.Builder builder = Category.Builder.newCategory(name);
        if (catLabels != null) {
          builder.addAttribute("label", catLabels[i]);
        }
        categories.add(builder.build());
        i++;
      }
    } catch (REXPMismatchException e) {
      // ignore
      log.warn("Error while parsing variable categories: {}", colname, e);
    }
    return categories;
  }

  @Override
  public Variable getVariable() {
    return variable;
  }

  @Override
  public ValueType getValueType() {
    return variable.getValueType();
  }

  @Override
  public Iterable<Value> getValues(SortedSet<VariableEntity> entities) {
    return null;
  }

  @Override
  public Value getValue(ValueSet valueSet) {
    REXP rexp = ((RValueSet) valueSet).getREXP();
    if (rexp instanceof REXPGenericVector) {
      REXPGenericVector tibble = (REXPGenericVector) rexp;
      RList vectors = tibble.asList();
      REXP vector = (REXP) vectors.get(position - 1);
      try {
        String strValue = vector.asString();
        return "NaN".equals(strValue) ? variable.getValueType().nullValue() : variable.getValueType().valueOf(strValue);
      } catch (REXPMismatchException e) {
        return variable.getValueType().nullValue();
      }
    }
    // TODO extract values at variable position
    return variable.getValueType().nullValue();
  }

  @Override
  public boolean supportVectorSource() {
    return true;
  }

  @Override
  public VectorSource asVectorSource() throws VectorSourceNotSupportedException {
    return this;
  }
}
