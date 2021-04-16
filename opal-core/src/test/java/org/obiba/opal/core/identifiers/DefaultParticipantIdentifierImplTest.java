/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.identifiers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.tools.LuhnValidator;

import static org.fest.assertions.api.Assertions.assertThat;

public class DefaultParticipantIdentifierImplTest {

  private IdentifierGeneratorImpl participantIdentifier;

  @Before
  public void setUp() throws Exception {
    participantIdentifier = new IdentifierGeneratorImpl();
  }

  @Test
  public void testGenerateParticipantIdentifierNotNull() {
    assertThat(participantIdentifier.generateIdentifier()).isNotNull();
  }

  @Test
  public void testGenerateParticipantIdentifierHasCorrectLength() {
    assertThat(participantIdentifier.generateIdentifier().length()).isEqualTo(10);
  }

  @Test
  public void testGenerateParticipantIdentifierDoesntStartWithZero() {
    // Only run test if the instance is configured that way.
    Assume.assumeFalse(participantIdentifier.isAllowStartWithZero());
    for(int i = 0; i < 10000; i++) { // Generate 10000 ids.
      assertThat(participantIdentifier.generateIdentifier().charAt(0)).isNotEqualTo('0')
          .overridingErrorMessage("Participant Identifier not expected to start with '0'");
    }
  }

  @Test
  public void testRandomDistributionForGeneratedIdentifiers() {
    // Allow start with zero to obtain a uniform distribution.
    participantIdentifier.setAllowStartWithZero(true);

    Map<Character, Integer> distributionMap = new HashMap<>();
    for(int i = 0; i < 10000; i++) { // Generate 10000 ids.
      String id = participantIdentifier.generateIdentifier();
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
      assertThat(c.getValue() >= 9500 && c.getValue() <= 10500).isTrue()
          .overridingErrorMessage("The distribution of [" + c.getKey() + "] has the value [" + c.getValue() + "].");
    }
  }

  @Test
  public void testLuhnValidIdentifierGeneration() {
    participantIdentifier.setKeySize(15);
    participantIdentifier.setWithCheckDigit(true);
    assertThat(LuhnValidator.validate(participantIdentifier.generateIdentifier())).isTrue();
    participantIdentifier.setKeySize(12);
    participantIdentifier.setWithCheckDigit(true);
    assertThat(LuhnValidator.validate(participantIdentifier.generateIdentifier())).isTrue();
  }
}
