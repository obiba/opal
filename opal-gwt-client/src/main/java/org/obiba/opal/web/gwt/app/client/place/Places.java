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

public final class Places {

  public static final String dashboard = "!dashboard";

  public static final Place dashboardPlace = new Place(dashboard);

  public static String dashboard() {
    return dashboard;
  }

  public static final String navigator = "!navigator";

  public static final Place navigatorPlace = new Place(navigator);

  public static String navigator() {
    return navigator;
  }

  public static final String units = "!units";

  public static final Place unitsPlace = new Place(units);

  public static String units() {
    return units;
  }

  public static final String files = "!files";

  public static final Place filesPlace = new Place(files);

  public static String files() {
    return files;
  }

  public static final String reportTemplates = "!reports";

  public static final Place reportTemplatesPlace = new Place(reportTemplates);

  public static String reportTemplates() {
    return reportTemplates;
  }

  public static final String jobs = "!jobs";

  public static final Place jobsPlace = new Place(jobs);

  public static String jobs() {
    return jobs;
  }

  public static final String administration = "!admin";

  public static final Place administrationPlace = new Place(administration);

  public static String administration() {
    return administration;
  }

  public static final class Place extends com.google.gwt.place.shared.Place {

    final String place;

    private Place(String name) {
      place = name;
    }

    public String getName() {
      return place;
    }
  }

}
