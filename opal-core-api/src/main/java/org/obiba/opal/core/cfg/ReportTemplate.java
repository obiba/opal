/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.cfg;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ReportTemplate {
  //
  // Instance Variables
  //

  private String name;

  private String design;

  private String format;

  private final Map<String, String> parameters;

  private String schedule;

  private final Set<String> emailNotificationAddresses;

  //
  // Constructors
  //

  public ReportTemplate() {
    parameters = Maps.newHashMap();
    emailNotificationAddresses = Sets.newHashSet();
  }

  //
  // Methods
  //

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDesign() {
    return design;
  }

  public void setDesign(String design) {
    this.design = design;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public Map<String, String> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, String> parameters) {
    this.parameters.clear();
    if(parameters != null) {
      this.parameters.putAll(parameters);
    }
  }

  public String getSchedule() {
    return schedule;
  }

  public void setSchedule(String schedule) {
    this.schedule = schedule;
  }

  public Set<String> getEmailNotificationAddresses() {
    return emailNotificationAddresses;
  }

  public void setEmailNotificationAddresses(Set<String> emailNotificationAddresses) {
    this.emailNotificationAddresses.clear();
    if(emailNotificationAddresses != null) {
      this.emailNotificationAddresses.addAll(emailNotificationAddresses);
    }
  }
}
