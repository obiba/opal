/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.domain.participant;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class ParticipantTest {

  Participant participant;

  @Before
  public void setUp() throws Exception {
    participant = new Participant();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddEntryWithNullCollectionCentreName() {
    participant.addEntry(null, "uniqueId");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddEntryWithNullUniqueIdentifyingKey() {
    participant.addEntry("Onyx", null);
  }

  @Test
  public void testGetKey() {
    participant.addEntry("Onyx", "OnyxUniqueId");
    participant.addEntry("DCC", "DCCUniqueId");

    Assert.assertEquals("OnyxUniqueId", participant.getKey("Onyx"));
    Assert.assertEquals("DCCUniqueId", participant.getKey("DCC"));
    Assert.assertNull(participant.getKey("KeyNotInMap"));
  }

  @Test
  public void testGetKeyWithNullValuesSupplied() {
    Assert.assertEquals(null, participant.getKey(null));
  }

  @Test
  public void testHasEntry() {
    participant.addEntry("Onyx", "OnyxUniqueId");

    Assert.assertTrue(participant.hasEntry("Onyx", "OnyxUniqueId"));
    Assert.assertFalse(participant.hasEntry("Onyx", "FakeId"));
    Assert.assertFalse(participant.hasEntry("FakeCollectionCenter", "OnyxUniqueId"));
    Assert.assertFalse(participant.hasEntry("FakeCollectionCenter", "FakeId"));
  }

  @Test
  public void testHasEntryWithNullValuesSupplied() {
    participant.addEntry("Onyx", "OnyxUniqueId");

    Assert.assertFalse(participant.hasEntry("Onyx", null));
    Assert.assertFalse(participant.hasEntry(null, "OnyxUniqueId"));
    Assert.assertFalse(participant.hasEntry(null, null));
  }
}
