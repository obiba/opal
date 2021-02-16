/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.domain;

public class RockAppConfig extends AppConfig {

  private AppCredentials administratorCredentials;
  private AppCredentials managerCredentials;
  private AppCredentials userCredentials;

  public RockAppConfig() {
  }

  public RockAppConfig(String host) {
    super(host);
  }

  public boolean hasAdministratorCredentials() {
    return administratorCredentials != null;
  }

  public void setAdministratorCredentials(AppCredentials administrator) {
    this.administratorCredentials = administrator;
  }

  public AppCredentials getAdministratorCredentials() {
    return administratorCredentials;
  }

  public boolean hasManagerCredentials() {
    return managerCredentials != null;
  }

  public void setManagerCredentials(AppCredentials manager) {
    this.managerCredentials = manager;
  }


  public AppCredentials getManagerCredentials() {
    return managerCredentials;
  }

  public boolean hasUserCredentials() {
    return userCredentials != null;
  }

  public void setUserCredentials(AppCredentials user) {
    this.userCredentials = user;
  }

  public AppCredentials getUserCredentials() {
    return userCredentials;
  }
}
