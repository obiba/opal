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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Locale;

import org.obiba.core.util.StreamUtil;
import org.obiba.onyx.engine.variable.Attribute;
import org.obiba.onyx.engine.variable.Category;
import org.obiba.onyx.engine.variable.IVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.util.VariableStreamer;
import org.obiba.opal.core.domain.metadata.Catalogue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.core.io.Resource;

/**
 *
 */
public class OnyxCatalogueReader implements ItemStreamReader<Catalogue> {

  private static final Logger log = LoggerFactory.getLogger(OnyxCatalogueReader.class);

  private static final String VARIABLES_FILE = "variables.xml";

  private Resource resource;

  private IOnyxDataInputStrategy dataInputStrategy;

  private IVariablePathNamingStrategy pathNamingStrategy;

  private OnyxDataInputContext dataInputContext;

  private Iterator<Variable> catalogues;

  public void setDataInputStrategy(IOnyxDataInputStrategy dataInputStrategy) {
    this.dataInputStrategy = dataInputStrategy;
  }

  public void setPathNamingStrategy(IVariablePathNamingStrategy pathNamingStrategy) {
    this.pathNamingStrategy = pathNamingStrategy;
  }

  public void setResource(Resource resource) {
    this.resource = resource;
  }

  public Catalogue read() throws Exception, UnexpectedInputException, ParseException {
    if(catalogues.hasNext() == false) {
      return null;
    }

    // TODO: we should probably not make one catalogue per DEF. There should be one catalogue for the whole
    // variables.xml file.

    Variable catalogueVariable = catalogues.next();
    Catalogue catalogue = new Catalogue(catalogueVariable.getName(), "onyx");
    doAddVariable(catalogue, catalogueVariable);
    return catalogue;
  }

  protected void doAddVariable(Catalogue catalogue, Variable variable) {
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

    for(Variable child : variable.getVariables()) {
      doAddVariable(catalogue, child);
    }

    for(Category child : variable.getCategories()) {
      doAddVariable(catalogue, child);
    }
  }

  public void close() throws ItemStreamException {
    dataInputStrategy.terminate(dataInputContext);
  }

  public void open(ExecutionContext executionContext) throws ItemStreamException {
    dataInputContext = new OnyxDataInputContext();
    try {
      dataInputContext.setSource(resource.getFile().getPath());
    } catch(IOException e) {
      throw new ItemStreamException(e);
    }
    dataInputStrategy.prepare(dataInputContext);

    Variable root = getVariableFromXmlFile(VARIABLES_FILE);
    // Each first-level variable becomes a catalogue.
    this.catalogues = root.getVariables().iterator();
  }

  public void update(ExecutionContext executionContext) throws ItemStreamException {
  }

  /**
   * Converts an XML file into a Variable or VariableDataSet.
   * @param <T> The type to be returned, such as Variable or VariableDataSet.
   * @param filename The XML file to be converted.
   * @return The root Variable or VariableDataSet
   */
  private <T> T getVariableFromXmlFile(String filename) {
    log.debug("getVariableFromXmlFile({})", filename);
    InputStream inputStream = null;
    T object = null;
    try {
      inputStream = dataInputStrategy.getEntry(filename);
      object = VariableStreamer.<T> fromXML(inputStream);
      if(object == null) {
        throw new IllegalStateException("Unable to load variables from the file [" + filename + "].");
      }
    } finally {
      StreamUtil.silentSafeClose(inputStream);
    }
    return object;
  }

}
