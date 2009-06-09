package org.obiba.opal.datasource.onyx.elmo;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.engine.variable.impl.DefaultVariablePathNamingStrategy;
import org.obiba.onyx.util.data.Data;
import org.obiba.opal.datasource.onyx.openrdf.util.DataTypeUtil;
import org.obiba.opal.datasource.onyx.variable.DefaultVariableQNameStrategy;
import org.obiba.opal.datasource.onyx.variable.IVariableQNameStrategy;
import org.obiba.opal.datasource.onyx.variable.VariableDataVisitor;
import org.obiba.opal.elmo.OpalOntologyManager;
import org.obiba.opal.elmo.concepts.CategoricalVariable;
import org.obiba.opal.elmo.concepts.Category;
import org.obiba.opal.elmo.concepts.ContinuousVariable;
import org.obiba.opal.elmo.concepts.DataVariable;
import org.obiba.opal.elmo.concepts.Dataset;
import org.obiba.opal.elmo.concepts.MissingCategory;
import org.obiba.opal.elmo.concepts.OccurrenceItem;
import org.obiba.opal.elmo.concepts.Participant;
import org.openrdf.OpenRDFException;
import org.openrdf.concepts.owl.Class;
import org.openrdf.elmo.ElmoQuery;
import org.openrdf.elmo.sesame.SesameManager;

public class ElmoVariableDataVisitor implements VariableDataVisitor {

  final OpalOntologyManager opal;

  final SesameManager manager;

  final List<Handler> handlers = new LinkedList<Handler>();

  final IVariableQNameStrategy qnameStrategy;

  private Dataset currentDataset;

  public ElmoVariableDataVisitor(String base, SesameManager manager) throws OpenRDFException, IOException {
    this.manager = manager;
    this.opal = new OpalOntologyManager();
    qnameStrategy = new DefaultVariableQNameStrategy(base, new DefaultVariablePathNamingStrategy());
    handlers.add(new CategoryHandler());
    handlers.add(new CategoricalHandler());
    handlers.add(new ContinuousHandler());
    handlers.add(new OccurrenceHandler());
  }

  public void forParticipant(String id) {
    System.out.println("Loading data for participant " + id);
    Participant e = manager.create(Participant.class);
    e.setIdentifier(id);
    currentDataset = manager.create(Dataset.class);
    currentDataset.setForEntity(e);
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
        break;
      }
    }

    for(VariableData child : data.getVariableDatas()) {
      visit(child);
    }
  }

  protected void handleOccurrence(VariableData data, DataVariable occurrenceInstance) {
    QName occurrence = qnameStrategy.getOccurenceVariable(data.getVariablePath());
    if(occurrence != null) {
      String id = qnameStrategy.getOccurenceValue(data.getVariablePath());
      OccurrenceItem repeated = findOccurrence(occurrence, id);
      if(repeated == null) {
        System.out.println("Cannot find occurrence " + id + " for variable " + occurrence);
        return;
      }
      repeated.getHasOccurrenceData().add(occurrenceInstance);
    }
  }

  protected OccurrenceItem findOccurrence(QName occurrence, String id) {
    ElmoQuery query = manager.createNativeQuery("SELECT o FROM {o} <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> {variable}, {o} <http://www.obiba.org/owl/2009/05/opal#identifier> {id}");
    query.setParameter("id", id);
    query.setQName("variable", occurrence);
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
        QName qname = qnameStrategy.getQName(data.getVariablePath(), d.getValueAsString());

        Class categoryVariable = manager.find(Class.class, qname);
        if(categoryVariable == null) {
          System.out.println("Cannot find Category " + qname);
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
