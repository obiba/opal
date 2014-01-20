/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.place;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class Places {

  public static final String LOGIN = "!login";

  public static final String INSTALL = "!install";

  public static final String DASHBOARD = "!dashboard";

  public static final String PROJECTS = "!projects";

  public static final String PROJECT = "!project";

  public static final String IDENTIFIERS = "!identifiers";

  public static final String UNITS = "!units";

  public static final String UNIT = "!unit";

  public static final String FILES = "!files";

  public static final String REPORT_TEMPLATES = "!reports";

  public static final String JOBS = "!jobs";

  public static final String ADMIN = "!adminpage";

  public static final String ADMINISTRATION = "!admin";

  public static final String USERS = ADMINISTRATION + ".users";

  public static final String PROFILES = "!profiles";

  public static final String DATABASES = "!databases";

  public static final String INDEX = ADMINISTRATION + ".index";

  public static final String DATASHIELD = ADMINISTRATION + ".datashield";

  public static final String R = ADMINISTRATION + ".r";

  public static final String JVM = ADMINISTRATION + ".jvm";

  public static final String SERVER = ADMINISTRATION + ".server";

  public static final String TAXONOMIES = ADMINISTRATION + ".taxonomies";

  public static final String TAXONOMY = ADMINISTRATION + ".taxonomy";

  public static final String VOCABULARY = ADMINISTRATION + ".vocabulary";

  public static final String VOCABULARY_EDIT = ADMINISTRATION + ".vocabulary.edit";

  private Places() {}

  public static final class Place extends com.google.gwt.place.shared.Place {

    private final String place;

    private final Map<String, String> params = new HashMap<String, String>();

    public Place(String name) {
      place = name;
    }

    public String getName() {
      return place;
    }

    public Place addParam(String name, String value) {
      params.put(name, value);
      return this;
    }

    public Set<String> getParameterNames() {
      return params.keySet();
    }

    public String getParameter(String key, String defaultValue) {
      String value = params.get(key);
      return value == null ? defaultValue : value;
    }
  }

}
