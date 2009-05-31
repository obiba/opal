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

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.domain.participant.identifier.IParticipantIdentifier;

public class DefaultParticipantIdentifierImplTest {

  private IParticipantIdentifier participantIdentifier;

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
  public void testRandomDistributionForGeneratedIdentifiers() {
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
    Set<Character> keys = distributionMap.keySet();
    for(Character c : keys) {
      // All characters should be used roughly the same number of times
      Assert.assertTrue("The distribution of [" + c + "] has the value [" + distributionMap.get(c) + "].", distributionMap.get(c) >= 9700 && distributionMap.get(c) <= 10300);
    }
  }
}
