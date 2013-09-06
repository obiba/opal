package org.obiba.opal.core.cfg;

import java.util.Date;

import com.orientechnologies.orient.core.hook.ORecordHookAbstract;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.impl.ODocument;

@SuppressWarnings("OverlyStrongTypeCast")
public class TimestampedHook extends ORecordHookAbstract {

  @Override
  public RESULT onRecordBeforeCreate(ORecord<?> record) {
    if(record instanceof ODocument) {
      ((ODocument) record).field("created", new Date());
      return RESULT.RECORD_CHANGED;
    }
    return RESULT.RECORD_NOT_CHANGED;
  }

  @Override
  public RESULT onRecordBeforeUpdate(ORecord<?> record) {
    if(record.isDirty() && record instanceof ODocument) {
      ((ODocument) record).field("updated", new Date());
      return RESULT.RECORD_CHANGED;
    }
    return RESULT.RECORD_NOT_CHANGED;
  }
}
