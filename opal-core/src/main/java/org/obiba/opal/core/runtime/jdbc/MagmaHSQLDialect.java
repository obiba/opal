package org.obiba.opal.core.runtime.jdbc;

import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Overrides the HSQLDialect to force the use of longvarchar for clobs
 */
public class MagmaHSQLDialect extends org.hibernate.dialect.HSQLDialect {

  private static final Logger log = LoggerFactory.getLogger(MagmaHSQLDialect.class);

  public MagmaHSQLDialect() {
    super();
    // Force the use of longvarchar and longvarbinary for clobs/blobs
    registerColumnType(Types.BLOB, "longvarbinary");
    registerColumnType(Types.CLOB, "longvarchar");
  }
}
