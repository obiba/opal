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

import java.util.Collection;

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

  @Test(expected = IllegalStateException.class)
  public void testAddEntryWhenAddingDuplicates() {
    participant.addEntry("Onyx", "uniqueId");
    participant.addEntry("Onyx", "uniqueId");
  }

  @Test
  public void testGetKey() {
    participant.addEntry("Onyx", "OnyxUniqueId");
    participant.addEntry("DCC", "DCCUniqueId");

    Assert.assertTrue(participant.getKey("Onyx").contains("OnyxUniqueId"));
    Assert.assertTrue(participant.getKey("DCC").contains("DCCUniqueId"));
    Assert.assertEquals(0, participant.getKey("KeyNotInMap").size());
  }

  @Test
  public void testGetKeyContainsMultipleValues() {
    participant.addEntry("BioBank", "TubeOne");
    participant.addEntry("BioBank", "TubeTwo");
    participant.addEntry("BioBank", "TubeThree");

    Collection<String> keys = participant.getKey("BioBank");

    Assert.assertEquals(3, keys.size());
    Assert.assertTrue(keys.contains("TubeOne"));
    Assert.assertTrue(keys.contains("TubeTwo"));
    Assert.assertTrue(keys.contains("TubeThree"));
  }

  @Test
  public void testGetKeyWithNullValuesSupplied() {
    Assert.assertEquals(0, participant.getKey(null).size());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testImmutablityOfReturnedCollection() {
    participant.addEntry("BioBank", "TubeTwo");

    participant.getKey("BioBank").remove("TubeTwo"); // Remove is not permitted.
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
