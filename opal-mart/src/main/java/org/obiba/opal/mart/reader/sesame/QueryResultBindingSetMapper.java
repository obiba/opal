package org.obiba.opal.mart.reader.sesame;

import javax.xml.namespace.QName;

import org.obiba.opal.elmo.concepts.DataItem;
import org.obiba.opal.mart.QueryResult;
import org.openrdf.elmo.sesame.SesameManager;
import org.openrdf.elmo.sesame.SesameManagerFactory;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.springframework.beans.factory.DisposableBean;

public class QueryResultBindingSetMapper implements BindingSetMapper<QueryResult>, DisposableBean {

  private SesameManagerFactory managerFactory;

  private SesameManager manager;

  public QueryResult mapBindingSet(BindingSet bindingSet, int setNum) {
    if(manager == null) {
      manager = managerFactory.createElmoManager();
    }
    return new BindingSetQueryResult(bindingSet);
  }

  public void setManager(SesameManagerFactory managerFactory) {
    this.managerFactory = managerFactory;
  }
  
  public void destroy() throws Exception {
    if(manager != null) {
      manager.close();
    }
  }

  private class BindingSetQueryResult implements QueryResult {

    private BindingSet bindingSet;

    public BindingSetQueryResult(BindingSet bindingSet) {
      this.bindingSet = bindingSet;
    }

    public DataItem getDataItemClass() {
      Binding varBinding = bindingSet.getBinding("var");
      URI varURI = (URI) varBinding.getValue();
      QName varQName = new QName(varURI.getNamespace(), varURI.getLocalName());
      return manager.find(DataItem.class, varQName);
    }

    public int getOccurrence() {
      Binding occurrence = bindingSet.getBinding("occ");
      if(occurrence == null) {
        return 0;
      }
      return (Integer) manager.getLiteralManager().getObject((Literal) occurrence.getValue());
    }

    public String getEntityId() {
      return bindingSet.getBinding("sid").getValue().stringValue();
    }

    public Object getValue() {
      Binding value = bindingSet.getBinding("value");
      if(value == null) {
        return null;
      }
      return manager.getLiteralManager().getObject((Literal) value.getValue());
    }

    public boolean hasOccurrence() {
      Binding occurrence = bindingSet.getBinding("occ");
      return occurrence != null;
    }
  }
}
