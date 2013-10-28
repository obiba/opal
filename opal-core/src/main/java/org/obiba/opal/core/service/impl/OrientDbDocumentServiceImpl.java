package org.obiba.opal.core.service.impl;

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.obiba.opal.core.service.OrientDbServerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.tx.OTransaction;

@Component
public class OrientDbDocumentServiceImpl implements OrientDbDocumentService {

  @Autowired
  private DefaultBeanValidator defaultBeanValidator;

  @Autowired
  private OrientDbServerFactory serverFactory;

  private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

  public <T> T execute(WithinDocumentTxCallback<T> callback) {
    ODatabaseDocumentTx db = serverFactory.getDocumentTx();
    try {
      return callback.withinDocumentTx(db);
    } catch(OException e) {
      db.rollback();
      throw e;
    } finally {
      db.close();
    }
  }

  @Override
  public <T> void save(T t) throws ConstraintViolationException {
    defaultBeanValidator.validate(t);

    ODatabaseDocumentTx db = serverFactory.getDocumentTx();
    try {
      //noinspection TypeMayBeWeakened
      ODocument document = new ODocument(t.getClass().getSimpleName());
      document.fromJSON(gson.toJson(t));

      db.begin(OTransaction.TXTYPE.OPTIMISTIC);
      document.save();
      db.commit();
    } catch(OException e) {
      db.rollback();
      throw e;
    } finally {
      db.close();
    }
  }

  @Override
  public <T> Iterable<T> list(Class<T> clazz) {
    ODatabaseDocumentTx db = serverFactory.getDocumentTx();
    try {
      return fromDocuments(db.browseClass(clazz.getSimpleName()), clazz);
    } finally {
      db.close();
    }
  }

  @Override
  public <T> Iterable<T> list(Class<T> clazz, String sql, Object... params) {
    ODatabaseDocumentTx db = serverFactory.getDocumentTx();
    try {
      return fromDocuments(db.<List<ODocument>>query(new OSQLSynchQuery<ODocument>(sql), params), clazz);
    } finally {
      db.close();
    }
  }

  private <T> Iterable<T> fromDocuments(Iterable<ODocument> documents, final Class<T> clazz) {
    return Iterables.transform(documents, new Function<ODocument, T>() {
      @Override
      public T apply(ODocument document) {
        return gson.fromJson(document.toJSON(), clazz);
      }
    });
  }

  @Override
  public <T> long count(Class<T> clazz) {
    ODatabaseDocumentTx db = serverFactory.getDocumentTx();
    try {
      return db.countClass(clazz.getSimpleName());
    } finally {
      db.close();
    }
  }

}
