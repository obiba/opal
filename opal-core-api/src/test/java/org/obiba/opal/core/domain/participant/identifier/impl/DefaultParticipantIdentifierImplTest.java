/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.domain.participant.identifier.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class DefaultParticipantIdentifierImplTest {

  private DefaultParticipantIdentifierImpl participantIdentifier;

  @Before
  public void setUp() throws Exception {
    participantIdentifier = new DefaultParticipantIdentifierImpl();
  }

  @Test
  public void testGenerateParticipantIdentifierNotNull() {
    Assert.assertNotNull(participantIdentifier.generateParticipantIdentifier());
  }

  @Test
  public void testGenerateParticipantIdentifierHasCorrectLength() {
    Assert.assertEquals(10, participantIdentifier.generateParticipantIdentifier().length());
  }

  @Test
  public void testGenerateParticipantIdentifierDoesntStartWithZero() {
    // Only run test if the instance is configured that way.
    Assume.assumeTrue(participantIdentifier.isAllowStartWithZero() == false);
    for(int i = 0; i < 10000; i++) { // Generate 10000 ids.
      Assert.assertTrue("Participant Identifier not expected to start with '0'", participantIdentifier.generateParticipantIdentifier().charAt(0) != '0');
    }
  }

  @Test
  public void testRandomDistributionForGeneratedIdentifiers() {
    // Allow start with zero to obtain a uniform distribution.
    participantIdentifier.setAllowStartWithZero(true);

    Map<Character, Integer> distributionMap = new HashMap<Character, Integer>();
    for(int i = 0; i < 10000; i++) { // Generate 10000 ids.
      String id = participantIdentifier.generateParticipantIdentifier();
      // Count occurrences of each of the 10 possible digits in each id.
      for(int j = 0; j < id.length(); j++) {
        char c = id.charAt(j);
        if(distributionMap.containsKey(c)) {
          int count = distributionMap.get(c);
          distributionMap.put(c, ++count);
        } else {
          distributionMap.put(c, 1);
        }
      }
    }
    Set<Entry<Character, Integer>> entries = distributionMap.entrySet();
    for(Entry<Character, Integer> c : entries) {
      // All characters should be used roughly the same number of times
      Assert.assertTrue("The distribution of [" + c.getKey() + "] has the value [" + c.getValue() + "].", c.getValue() >= 9500 && c.getValue() <= 10500);
    }
  }
}
