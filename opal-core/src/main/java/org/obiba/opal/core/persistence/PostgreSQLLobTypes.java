/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.persistence;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.jdbc.LongVarbinaryJdbcType;
import org.hibernate.type.descriptor.jdbc.LongVarcharJdbcType;
import org.hibernate.type.descriptor.jdbc.spi.JdbcTypeRegistry;

/**
 * Maps {@code @Lob} to {@code text} and {@code bytea} on PostgreSQL.
 * <p>
 * The configuration model keeps its nested beans, collections and long strings in {@code @Lob} columns, which the
 * Liquibase changelog declares as {@code CLOB}, and the keystore in one it declares as {@code bytea} on PostgreSQL
 * and {@code BLOB} elsewhere. So on PostgreSQL they are {@code text} and {@code bytea}, the ordinary types there.
 * Hibernate, on the other hand, maps {@code @Lob} on PostgreSQL
 * to {@code oid}, a reference to a server-side large object. Schema validation therefore refuses the columns Liquibase
 * made ("found [text (Types#VARCHAR)], but expecting [oid (Types#CLOB)]"), and had it accepted them, every read and
 * write would have gone through the large-object API, which also leaves an orphan behind each update and drops out of
 * a {@code pg_dump} run without {@code -b}.
 * <p>
 * So on PostgreSQL the JDBC types registered for CLOB and BLOB are replaced with the ones the dialect uses for
 * {@code text} and {@code bytea}. The entities keep their {@code @Lob}: they are in opal-core-api, which is mapped
 * with JPA annotations alone, and {@code @Lob} is exactly right on H2, where {@code clob} is what Liquibase creates.
 * <p>
 * It is registered in {@code META-INF/services/org.hibernate.boot.model.TypeContributor} rather than through the
 * {@code hibernate.type_contributors} setting, and the difference matters: a contributor given as a setting is applied
 * the moment the metadata builder receives it, before the dialect registers its own types, and the PostgreSQL dialect
 * then puts its large-object bindings back over these. Contributors found as services run after the dialect.
 */
public class PostgreSQLLobTypes implements TypeContributor {

  @Override
  public void contribute(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
    Dialect dialect = serviceRegistry.getService(JdbcServices.class).getDialect();
    if(!(dialect instanceof PostgreSQLDialect)) return;
    JdbcTypeRegistry registry = typeContributions.getTypeConfiguration().getJdbcTypeRegistry();
    // The "long32" DDL codes are the ones PostgreSQLDialect renders as text and bytea. Registered under the LOB codes,
    // these descriptors are what a @Lob field resolves to, and their DDL code is what the validator compares to the
    // column. They read and write with getString/setString and getBytes/setBytes, as text and bytea want.
    registry.addDescriptor(SqlTypes.CLOB, new LongVarcharJdbcType(SqlTypes.LONG32VARCHAR));
    registry.addDescriptor(SqlTypes.BLOB, new LongVarbinaryJdbcType(SqlTypes.LONG32VARBINARY));
  }
}
