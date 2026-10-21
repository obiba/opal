/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.search.service.impl;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.Test;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityBean;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class EntitiesIndexTest {

  private static VariableEntity participant(String id) {
    return new VariableEntityBean("Participant", id);
  }

  private static void index(Directory directory, String tableReference, List<VariableEntity> entities) throws Exception {
    try (IndexWriter writer = TablesIndexManagerImpl.newEntitiesIndexWriter(directory)) {
      TablesIndexManagerImpl.writeEntities(writer, tableReference, entities);
      writer.commit();
    }
  }

  @Test
  public void test_find_and_reindex() throws Exception {
    Directory directory = new ByteBuffersDirectory();
    assertThat(TablesIndexManagerImpl.findTableReferences(directory, participant("1"))).isEmpty();

    index(directory, "p.t1", List.of(participant("1"), participant("1-2")));
    index(directory, "p.t2", List.of(participant("1"), new VariableEntityBean("Sample", "2")));

    assertThat(TablesIndexManagerImpl.findTableReferences(directory, participant("1"))).containsOnly("p.t1", "p.t2");
    // exact match on identifier and type
    assertThat(TablesIndexManagerImpl.findTableReferences(directory, participant("2"))).isEmpty();
    assertThat(TablesIndexManagerImpl.findTableReferences(directory, new VariableEntityBean("Sample", "2"))).containsOnly("p.t2");

    // reindexing replaces the entities of the table
    index(directory, "p.t2", List.of(participant("3")));
    assertThat(TablesIndexManagerImpl.findTableReferences(directory, participant("1"))).containsOnly("p.t1");
    assertThat(TablesIndexManagerImpl.findTableReferences(directory, participant("3"))).containsOnly("p.t2");
  }
}
