/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.vcs.git;

public class NoSuchGitRepositoryException extends RuntimeException {

  private final String gitRepositoryName;

  public NoSuchGitRepositoryException(String gitRepositoryName) {
    super("No such git repository: " + gitRepositoryName);
    this.gitRepositoryName = gitRepositoryName;
  }

  public String getGitRepositoryName() {
    return gitRepositoryName;
  }
}
