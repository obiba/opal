package org.obiba.opal.datasource.onyx.elmo;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.util.data.Data;
import org.obiba.opal.datasource.onyx.openrdf.util.DataTypeUtil;
import org.obiba.opal.datasource.onyx.variable.IVariableQNameStrategy;
import org.obiba.opal.datasource.onyx.variable.VariableDataVisitor;
import org.obiba.opal.elmo.OpalOntologyManager;
import org.obiba.opal.elmo.concepts.CategoricalVariable;
import org.obiba.opal.elmo.concepts.Category;
import org.obiba.opal.elmo.concepts.ContinuousVariable;
import org.obiba.opal.elmo.concepts.DataVariable;
import org.obiba.opal.elmo.concepts.Dataset;
import org.obiba.opal.elmo.concepts.Entity;
import org.obiba.opal.elmo.concepts.MissingCategory;
import org.obiba.opal.elmo.concepts.OccurrenceItem;
import org.openrdf.OpenRDFException;
import org.openrdf.concepts.owl.Class;
import org.openrdf.elmo.ElmoQuery;
import org.openrdf.elmo.sesame.SesameManager;
import org.openrdf.elmo.sesame.SesameManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElmoVariableDataVisitor implements VariableDataVisitor {

  private static final Logger log = LoggerFactory.getLogger(ElmoVariableDataVisitor.class);

  final OpalOntologyManager opal;

  final SesameManagerFactory managerFactory;

  final List<Handler> handlers = new LinkedList<Handler>();

  final IVariableQNameStrategy qnameStrategy;

  private Dataset currentDataset;

  private SesameManager manager;

  public ElmoVariableDataVisitor(IVariableQNameStrategy qnameStrategy, SesameManagerFactory managerFactory) throws OpenRDFException, IOException {
    this.managerFactory = managerFactory;
    this.opal = new OpalOntologyManager();
    this.qnameStrategy = qnameStrategy;
    handlers.add(new CategoryHandler());
    handlers.add(new CategoricalHandler());
    handlers.add(new ContinuousHandler());
    handlers.add(new OccurrenceHandler());
  }

  public void forEntity(java.lang.Class<? extends Entity> entityType, String id, Date sourceDate) {
    if(manager == null) {
      this.manager = managerFactory.createElmoManager();
      this.manager.getTransaction().begin();
    } else {
      this.manager.flush();
    }
    log.info("Loading data for participant {}", id);

    Entity entity = null;
    for(Entity e : manager.findAll(entityType)) {
      if(id.equals(e.getIdentifier())) {
        entity = e;
        break;
      }
    }

    if(entity == null) {
      entity = manager.create(entityType);
      entity.setIdentifier(id);
    }
    currentDataset = manager.create(Dataset.class);

    currentDataset.setCreationDate(toXMLGregorianCalendar(new Date()));
    if(sourceDate != null) {
      currentDataset.setSourceDate(toXMLGregorianCalendar(sourceDate));
    }

    currentDataset.setForEntity(entity);
  }

  private XMLGregorianCalendar toXMLGregorianCalendar(Date date) {
    GregorianCalendar cal = new GregorianCalendar();
    cal.setTime(date);
    try {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
    } catch(DatatypeConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  public void visit(VariableData data) {
    QName qname = qnameStrategy.getQName(data.getVariablePath());
    Class opalOnyxVariable = manager.find(Class.class, qname);
    if(opalOnyxVariable == null) {
      throw new IllegalArgumentException("No variable for path " + data.getVariablePath() + " (QName " + qname + ")");
    }

    for(Handler h : handlers) {
      if(h.handles(opalOnyxVariable, data) == true) {
        h.handle(opalOnyxVariable, data);
        System.out.print('.');
        break;
      }
    }

    for(VariableData child : data.getVariableDatas()) {
      visit(child);
    }
  }

  public void end() {
    if(manager != null) {
      this.manager.getTransaction().commit();
      manager.close();
      manager = null;
    }
  }

  protected void handleOccurrence(VariableData data, DataVariable occurrenceInstance) {
    QName occurrence = qnameStrategy.getOccurenceVariable(data.getVariablePath());
    if(occurrence != null) {
      String id = qnameStrategy.getOccurenceIdentifier(data.getVariablePath());
      OccurrenceItem repeated = findOccurrence(occurrence, id);
      if(repeated == null) {
        log.warn("Cannot find occurrence {} for variable {}", id, occurrence);
        return;
      }
      repeated.getHasOccurrenceData().add(occurrenceInstance);
    }
  }

  protected OccurrenceItem findOccurrence(QName occurrence, String id) {
    ElmoQuery query = manager.createNativeQuery("SELECT o FROM {o} <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> {variable}, {o} <http://www.obiba.org/owl/2009/05/opal#identifier> {id}, {o} <http://www.obiba.org/owl/2009/05/opal#withinDataset> {ds}");
    query.setParameter("id", id);
    query.setQName("variable", occurrence);
    query.setParameter("ds", currentDataset);

    for(Object result : query.getResultList()) {
      return manager.designateEntity(result, OccurrenceItem.class);
    }
    return null;
  }

  protected <T> T findSingleton(QName qname, java.lang.Class<T> type) {
    ElmoQuery query = manager.createNativeQuery("SELECT s FROM {s} <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> {variable}");

    query.setQName("variable", qname);
    for(Object result : query.getResultList()) {
      return manager.designateEntity(result, type);
    }
    return null;
  }

  protected <T> T findSingleton(Class variable, java.lang.Class<T> type) {
    return findSingleton(variable.getQName(), type);
  }

  protected <T extends Class> T getSingleton(Class variable, java.lang.Class<T> type) {
    T c = findSingleton(variable.getQName(), type);
    if(c == null) {
      c = manager.create(type);
      c.setRdfTypes(Collections.singleton(variable));
    }
    return c;
  }

  public interface Handler {
    public boolean handles(Class opalOnyxVariable, VariableData data);

    public void handle(Class opalOnyxVariable, VariableData data);
  }

  public class CategoryHandler implements Handler {

    public void handle(Class opalOnyxVariable, VariableData data) {
      Category c = getSingleton(opalOnyxVariable, Category.class);
      c.getWithinDataset().add(currentDataset);
      handleOccurrence(data, c);
    }

    public boolean handles(Class opalOnyxVariable, VariableData data) {
      Set<?> subClassOf = opalOnyxVariable.getRdfsSubClassOf();
      return subClassOf.contains(opal.getOpalClass(Category.class)) || subClassOf.contains(opal.getOpalClass(MissingCategory.class));
    }

  }

  public class CategoricalHandler implements Handler {

    public void handle(Class opalOnyxVariable, VariableData data) {
      CategoricalVariable c = manager.create(CategoricalVariable.class);
      c.setRdfTypes(Collections.singleton(opalOnyxVariable));
      c.getWithinDataset().add(currentDataset);

      // Data of a categorical variable is the name of the chosen category
      for(Data d : data.getDatas()) {
        // Build the QName of the Category (child of this variable)
        QName qname = qnameStrategy.getChildQName(data.getVariablePath(), d.getValueAsString());

        Class categoryVariable = manager.find(Class.class, qname);
        if(categoryVariable == null) {
          log.warn("Cannot find Category {}", qname);
        } else {
          Category category = getSingleton(categoryVariable, Category.class);
          category.getWithinDataset().add(currentDataset);
          c.getHasCategory().add(category);
        }
      }
      handleOccurrence(data, c);
    }

    public boolean handles(Class opalOnyxVariable, VariableData data) {
      return opalOnyxVariable.getRdfsSubClassOf().contains(opal.getOpalClass(CategoricalVariable.class));
    }

  }

  public class ContinuousHandler implements Handler {

    public void handle(Class opalOnyxVariable, VariableData data) {
      ContinuousVariable c = manager.create(ContinuousVariable.class);
      c.setRdfTypes(Collections.singleton(opalOnyxVariable));
      c.getWithinDataset().add(currentDataset);
      for(Data d : data.getDatas()) {
        c.setValue(DataTypeUtil.getValue(d));
        break;
      }
      handleOccurrence(data, c);
    }

    public boolean handles(Class opalOnyxVariable, VariableData data) {
      return opalOnyxVariable.getRdfsSubClassOf().contains(opal.getOpalClass(ContinuousVariable.class));
    }

  }

  public class OccurrenceHandler implements Handler {

    public void handle(Class opalOnyxVariable, VariableData data) {
      int i = 1;
      for(Data d : data.getDatas()) {
        OccurrenceItem c = manager.create(OccurrenceItem.class);
        c.setRdfTypes(Collections.singleton(opalOnyxVariable));
        c.getWithinDataset().add(currentDataset);
        c.setIdentifier(d.getValueAsString());
        c.setOrdinal(i++);
      }
    }

    public boolean handles(Class opalOnyxVariable, VariableData data) {
      return opalOnyxVariable.getRdfsSubClassOf().contains(opal.getOpalClass(OccurrenceItem.class));
    }

  }
}
