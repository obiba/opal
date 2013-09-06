package org.obiba.opal.core.cfg;

import java.util.List;
import java.util.Map;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

public interface OrientDbService {

  <T> T execute(OrientTransactionCallback<T> action);

  List<ODocument> query(OSQLSynchQuery<ODocument> query, Map<String, Object> params);

}
