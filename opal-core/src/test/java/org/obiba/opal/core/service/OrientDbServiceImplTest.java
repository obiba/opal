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

import com.google.common.base.Throwables;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.domain.security.SubjectProfile;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;

@ContextConfiguration(classes = OrientDbServiceImplTest.Config.class)
public class OrientDbServiceImplTest  extends AbstractOrientdbServiceTest {

  @Autowired
  private OrientDbService orientDbService;

  private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  @Test
  public void testDateDeserialization () {
    Date date = parseDate("2015-01-01 00:00:00");

    try(ODatabaseDocument tx = orientDbServerFactory.getDocumentTx()) {
      ODocument document = new ODocument(SubjectProfile.class.getSimpleName());
      SubjectProfile profile = orientDbService
          .fromDocument(SubjectProfile.class, document.fromJSON("{\"created\": \""+ df.format(date) +"\"}"));

      assertThat(profile.getCreated().getTime()).isEqualTo(date.getTime());

      profile = orientDbService
          .fromDocument(SubjectProfile.class, document.fromJSON("{\"created\": " + date.getTime() + " }"));

      assertThat(profile.getCreated().getTime()).isEqualTo(date.getTime());
    }
  }

  @Test
  public void testDateSerialization() {
    Date date = parseDate("2015-01-01 00:00:00");
    SubjectProfile profile = new SubjectProfile();
    profile.setCreated(date);

    try(ODatabaseDocument tx = orientDbServerFactory.getDocumentTx()) {
      ODocument document = new ODocument(SubjectProfile.class.getSimpleName());
      orientDbService.copyToDocument(profile, document);
      assertThat(document.toJSON())
          .contains("\"" + df.format(date) + "\"");
    }
  }

  private Date parseDate(String date) {
    try {
      return df.parse(date);
    } catch(ParseException e) {
      throw new RuntimeException(e);
    }
  }

  @Configuration
  public static class Config extends AbstractOrientDbTestConfig {

  }
}
