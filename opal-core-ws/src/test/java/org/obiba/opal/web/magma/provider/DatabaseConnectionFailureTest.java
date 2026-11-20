/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.magma.provider;

import com.mongodb.MongoTimeoutException;
import org.junit.Test;
import org.obiba.magma.MagmaRuntimeException;

import java.sql.SQLException;

import static org.fest.assertions.api.Assertions.assertThat;

public class DatabaseConnectionFailureTest {

  @Test
  public void test_connection_failures_are_found_in_the_causes() {
    assertThat(DatabaseConnectionFailure.isConnectionFailure(
        new RuntimeException("wrapped", new SQLException("Connection refused", "08S01")))).isTrue();
    assertThat(DatabaseConnectionFailure.isConnectionFailure(
        new MagmaRuntimeException(new MongoTimeoutException("no server")))).isTrue();
  }

  @Test
  public void test_other_failures_are_not_connection_failures() {
    assertThat(DatabaseConnectionFailure.isConnectionFailure(new SQLException("syntax error", "42000"))).isFalse();
    assertThat(DatabaseConnectionFailure.isConnectionFailure(new SQLException("no state"))).isFalse();
    assertThat(DatabaseConnectionFailure.isConnectionFailure(new RuntimeException("other"))).isFalse();
  }

}
