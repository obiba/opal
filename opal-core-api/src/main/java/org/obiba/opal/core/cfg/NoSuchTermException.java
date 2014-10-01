/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.cfg;

public class NoSuchTermException extends RuntimeException {

  private static final long serialVersionUID = -4644188555401842279L;

  private final String vocabularyName;

  private final String termName;

  public NoSuchTermException(String vocabularyName, String termName) {
    super("No term exists in vocabulary '" + vocabularyName + "' with the specified name '" + termName + "'");
    this.vocabularyName = vocabularyName;
    this.termName = termName;
  }

  public String getVocabularyName() {
    return vocabularyName;
  }

  public String getTermName() {
    return termName;
  }
}
