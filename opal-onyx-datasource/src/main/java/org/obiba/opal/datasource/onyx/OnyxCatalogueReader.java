/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datasource.onyx;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Locale;

import org.obiba.onyx.engine.variable.Attribute;
import org.obiba.onyx.engine.variable.Category;
import org.obiba.onyx.engine.variable.IVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.opal.core.domain.metadata.Catalogue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

public class OnyxCatalogueReader extends AbstractOnyxReader<Catalogue> {

  private static final Logger log = LoggerFactory.getLogger(OnyxCatalogueReader.class);

  private IVariablePathNamingStrategy pathNamingStrategy;

  private String catalogueName;

  private Iterator<Variable> variables;

  public void setPathNamingStrategy(IVariablePathNamingStrategy pathNamingStrategy) {
    this.pathNamingStrategy = pathNamingStrategy;
  }

  public void setCatalogueName(String catalogueName) {
    this.catalogueName = catalogueName;
  }

  public void doOpen(ExecutionContext executionContext) throws ItemStreamException {
    Variable root = readVariables();
    // Each first-level variable becomes a catalogue.
    this.variables = root.getVariables().iterator();
  }

  public Catalogue read() throws Exception, UnexpectedInputException, ParseException {
    if(variables.hasNext() == false) {
      return null;
    }

    Catalogue catalogue = new Catalogue("onyx", catalogueName);

    while(variables.hasNext()) {
      doAddVariable(catalogue, variables.next());
    }
    return catalogue;
  }

  protected void doAddVariable(Catalogue catalogue, Variable variable) {
    log.debug("Processing variable {}", pathNamingStrategy.getPath(variable));
    org.obiba.opal.core.domain.metadata.Variable opalVar = catalogue.addVariable(pathNamingStrategy.getPath(variable));

    opalVar.addAttribute("dataType", variable.getDataType());
    opalVar.addAttribute("categorical", variable.isCategorial());
    opalVar.addAttribute("multiple", variable.isMultiple());
    opalVar.addAttribute("repeatable", variable.isRepeatable());
    opalVar.addAttribute("mimeType", variable.getMimeType());
    opalVar.addAttribute("unit", variable.getUnit());

    if(variable instanceof Category) {
      Category category = (Category) variable;
      opalVar.addAttribute("category", true);
      opalVar.addAttribute("escape", category.getEscape());
      opalVar.addAttribute("alternateName", category.getAlternateName());
    }

    for(Attribute a : variable.getAttributes()) {
      Serializable s = a.getValue();
      // TODO: maybe we should store the attribute's type (integer, decimal, etc.)
      Locale lc = a.getLocale();
      opalVar.addAttribute(a.getKey(), s != null ? s.toString() : null, lc);
    }

    for(Variable child : variable.getRegularVariables()) {
      doAddVariable(catalogue, child);
    }

    for(Category child : variable.getCategories()) {
      doAddVariable(catalogue, child);
    }
  }

}
