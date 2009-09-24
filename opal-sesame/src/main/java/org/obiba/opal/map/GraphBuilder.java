/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.map;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class GraphBuilder {

  private static final Logger log = LoggerFactory.getLogger(GraphBuilder.class);

  private Graph graph;

  private Resource resource;

  private boolean changed = false;

  public GraphBuilder(Graph graph) {
    this.graph = graph;
  }

  public GraphBuilder forResource(URI resource) {
    this.resource = resource;
    return this;
  }

  public Graph build() {
    return graph;
  }

  public Graph getCurrentGraph() {
    return graph;
  }

  public GraphBuilder withLiteral(URI predicate, String value) {
    return withLiteral(predicate, value, null);
  }

  public GraphBuilder withLiteral(URI predicate, String value, Locale locale) {
    return this.withLiteral(predicate, value, locale, "string");
  }

  public GraphBuilder withLiteral(URI predicate, String value, Locale locale, String type) {
    if(value == null) throw new IllegalArgumentException("Cannot add predicate '" + predicate + "': literal value cannot be null");
    Literal literal = null;
    if(locale != null) {
      // if it is localized, it is a string
      literal = graph.getValueFactory().createLiteral(value, locale.toString());
    } else if(type != null) {
      if(type.equalsIgnoreCase("boolean")) {
        literal = graph.getValueFactory().createLiteral(Boolean.parseBoolean(value));
      } else if(type.equalsIgnoreCase("byte")) {
        literal = graph.getValueFactory().createLiteral(Byte.parseByte(value));
      } else if(type.equalsIgnoreCase("float")) {
        literal = graph.getValueFactory().createLiteral(Float.parseFloat(value));
      } else if(type.equalsIgnoreCase("double")) {
        literal = graph.getValueFactory().createLiteral(Double.parseDouble(value));
      } else if(type.equalsIgnoreCase("integer")) {
        literal = graph.getValueFactory().createLiteral(Integer.parseInt(value));
      } else if(type.equalsIgnoreCase("long")) {
        literal = graph.getValueFactory().createLiteral(Long.parseLong(value));
      } else if(type.equalsIgnoreCase("date")) {
        try {
          GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance();
          calendar.setTime(SimpleDateFormat.getInstance().parse(value));
          literal = graph.getValueFactory().createLiteral(DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar));
        } catch(ParseException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch(DatatypeConfigurationException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      } else if(type.equalsIgnoreCase("string")) {
        literal = graph.getValueFactory().createLiteral(value);
      }
    }

    if(literal == null) {
      literal = graph.getValueFactory().createLiteral(value);
    }
    log.debug("adding statement {} {} {}", new Object[] { resource, predicate, literal });
    trackChange(graph.add(resource, predicate, literal));
    return this;
  }

  public void clearChanged() {
    changed = false;
  }

  public boolean hasChanged() {
    return changed;
  }

  public GraphBuilder withRelation(URI predicate, URI related) {
    log.debug("adding statement {} {} {}", new Object[] { resource, predicate, related });
    trackChange(graph.add(resource, predicate, related));
    return this;
  }

  public GraphBuilder withInverseRelation(URI predicate, URI related) {
    log.debug("adding statement {} {} {}", new Object[] { related, predicate, resource });
    trackChange(graph.add(related, predicate, resource));
    return this;
  }

  protected void trackChange(boolean change) {
    changed = changed || change;
    log.debug("graph changed {}", changed);
  }
}
