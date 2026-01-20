/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import org.junit.Test;
import org.obiba.opal.core.domain.security.SubjectProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;

@ContextConfiguration(classes = OrientDbServiceImplTest.Config.class)
public class OrientDbServiceImplTest extends AbstractOrientdbServiceTest {

  @Autowired
  private OrientDbService orientDbService;

  private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  @Test
  public void testDateDeserialization() {
    Date date = parseDate("2015-01-01 00:00:00");

    // Test date string deserialization
    SubjectProfile profile = orientDbService.fromJson("{\"created\": \"" + df.format(date) + "\"}", SubjectProfile.class);
    assertThat(profile.getCreated().getTime()).isEqualTo(date.getTime());

    // Test date long deserialization
    profile = orientDbService.fromJson("{\"created\": " + date.getTime() + " }", SubjectProfile.class);
    assertThat(profile.getCreated().getTime()).isEqualTo(date.getTime());
  }

  @Test
  public void testDateSerialization() {
    Date date = parseDate("2015-01-01 00:00:00");
    SubjectProfile profile = new SubjectProfile();
    profile.setCreated(date);

    String json = orientDbService.toJson(profile);
    assertThat(json).contains("\"" + df.format(date) + "\"");
  }

  private Date parseDate(String date) {
    try {
      return df.parse(date);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  @Configuration
  public static class Config extends AbstractOrientDbTestConfig {

  }
}
