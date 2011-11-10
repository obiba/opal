/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime.jdbc;

import java.io.IOException;
import java.util.Properties;

import org.hibernate.cfg.Environment;
import org.springframework.beans.factory.config.PropertiesFactoryBean;

public class HsqlDialectOverride extends PropertiesFactoryBean {

  @Override
  protected Properties createProperties() throws IOException {
    Properties props = super.createProperties();
    String dialect = props.getProperty(Environment.DIALECT);
    if(dialect != null && dialect.equals(org.hibernate.dialect.HSQLDialect.class.getName())) {
      props.setProperty(Environment.DIALECT, MagmaHSQLDialect.class.getName());
    }
    return props;
  }
}
