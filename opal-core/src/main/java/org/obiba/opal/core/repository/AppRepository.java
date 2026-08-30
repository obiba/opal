/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.repository;

import org.obiba.opal.core.runtime.App;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppRepository extends JpaRepository<App, String> {

  List<App> findByType(String type);

  List<App> findByNameAndTypeAndServer(String name, String type, String server);

  /**
   * An application registers itself with the identifier it was given, so it is already the key.
   */
  default App upsert(App app) {
    return save(app);
  }
}
