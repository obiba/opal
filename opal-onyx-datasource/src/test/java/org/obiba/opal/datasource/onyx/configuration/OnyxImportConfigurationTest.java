/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datasource.onyx.configuration;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 */
public class OnyxImportConfigurationTest {

  private OnyxImportConfiguration config;

  @Before
  public void setup() {
    config = new OnyxImportConfiguration();
  }

  @Test
  public void testIncludedVariablesAll() {
    config.setIncludeAll(true);
    // timestamps
    config.addFilteredOutVariables(".+Questionnaire.+TIMESTAMP_.*");
    config.addFilteredInVariables(".+Questionnaire.+TIMESTAMP.TIMESTAMP_.*");
    config.addFilteredInVariables(".+Questionnaire.+TIMESTAMP_.+active");
    // participant identification
    config.addFilteredOutVariables(".+participantCode");

    Assert.assertFalse(config.isIncluded("Onyx.HealthQuestionnaireTouchScreen.TIMESTAMP_TS_START"));
    Assert.assertFalse(config.isIncluded("Onyx.QuestionnaireNurse.TIMESTAMP_TS_END"));

    Assert.assertTrue(config.isIncluded("Onyx.HealthQuestionnaireTouchScreen.TIMESTAMP_TS_START.active"));

    Assert.assertFalse(config.isIncluded("Onyx.HealthQuestionnaireTouchScreen.TIMESTAMP_TS_START.TIMESTAMP"));

    Assert.assertTrue(config.isIncluded("Onyx.HealthQuestionnaireTouchScreen.TIMESTAMP_TS_START.TIMESTAMP.TIMESTAMP_TS_START"));
    Assert.assertTrue(config.isIncluded("Onyx.QuestionnaireNurse.TIMESTAMP_TS_END.TIMESTAMP.TIMESTAMP_TS_END"));

    Assert.assertFalse(config.isIncluded("Onyx.SamplesCollection.RegisteredParticipantTube.participantCode"));
  }

  @Test
  public void testIncludedVariablesNone() {
    config.setIncludeAll(false);
    // timestamps
    config.addFilteredInVariables(".+Questionnaire.+TIMESTAMP.TIMESTAMP_.*");

    Assert.assertFalse(config.isIncluded("Onyx.HealthQuestionnaireTouchScreen.TIMESTAMP_TS_START"));
    Assert.assertFalse(config.isIncluded("Onyx.QuestionnaireNurse.TIMESTAMP_TS_END"));

    Assert.assertFalse(config.isIncluded("Onyx.HealthQuestionnaireTouchScreen.TIMESTAMP_TS_START.active"));

    Assert.assertFalse(config.isIncluded("Onyx.HealthQuestionnaireTouchScreen.TIMESTAMP_TS_START.TIMESTAMP"));

    Assert.assertTrue(config.isIncluded("Onyx.HealthQuestionnaireTouchScreen.TIMESTAMP_TS_START.TIMESTAMP.TIMESTAMP_TS_START"));
    Assert.assertTrue(config.isIncluded("Onyx.QuestionnaireNurse.TIMESTAMP_TS_END.TIMESTAMP.TIMESTAMP_TS_END"));

    Assert.assertFalse(config.isIncluded("Onyx.SamplesCollection.RegisteredParticipantTube.participantCode"));
  }

  @Test
  public void testKeyVariables() {
    config.addKeyVariable("sampleQA", ".+RegisteredParticipantTube.barcode");

    Assert.assertTrue(config.isKeyVariable("Onyx.SamplesCollection.RegisteredParticipantTube.barcode"));
    Assert.assertEquals("sampleQA", config.getKeyVariableOwner("Onyx.SamplesCollection.RegisteredParticipantTube.barcode"));
  }

}
