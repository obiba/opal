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

  private Places() {}

  public static final String LOGIN = "!login";

  public static final Place LOGIN_PLACE = new Place(LOGIN);

  public static final String DASHBOARD = "!dashboard";

  public static final Place DASHBOARD_PLACE = new Place(DASHBOARD);

  public static final String PROJECTS = "!projects";

  public static final Place PROJECTS_PLACE = new Place(PROJECTS);

  public static final String PROJECT = "!project";

  public static final String NAVIGATOR = "!navigator";

  public static final Place NAVIGATOR_PLACE = new Place(NAVIGATOR);

  public static final String UNITS = "!units";

  public static final Place unitsPlace = new Place(UNITS);

  public static final String UNIT = "!unit";

  public static final Place UNIT_PLACE = new Place(UNIT);

  public static final String FILES = "!files";

  public static final Place FILES_PLACE = new Place(FILES);

  public static final String REPORT_TEMPLATES = "!reports";

  public static final Place REPORT_TEMPLATES_PLACE = new Place(REPORT_TEMPLATES);

  public static final String JOBS = "!jobs";

  public static final Place JOBS_PLACE = new Place(JOBS);

  public static final String ADMINISTRATION = "!admin";

  public static final Place ADMINISTRATION_PLACE = new Place(ADMINISTRATION);

  public static final String ADMIN = "!adminpage";

  public static final Place ADMIN_PLACE = new Place(ADMIN);

  public static final String USERS_GROUPS = ADMINISTRATION + ".users";

  public static final Place USERS_GROUPS_PLACE = new Place(USERS_GROUPS);

  public static final String DATABASES = "!admin.databases";

  public static final Place DATABASES_PLACE = new Place(DATABASES);

  public static final String SQL_DATABASES = "!admin.databases.sql";

  public static final Place SQL_DATABASES_PLACE = new Place(SQL_DATABASES);

  public static final String INDEX = ADMINISTRATION + ".index";

  public static final Place INDEX_PLACE = new Place(INDEX);

  public static final String DATASHIELD = ADMINISTRATION + ".datashield";

  public static final Place DATASHIELD_PLACE = new Place(DATASHIELD);

  public static final String R = ADMINISTRATION + ".r";

  public static final Place R_PLACE = new Place(R);

  public static final String JVM = ADMINISTRATION + ".jvm";

  public static final Place JVM_PLACE = new Place(JVM);

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
