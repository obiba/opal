/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.cfg;

public class NoSuchVocabularyException extends RuntimeException {

  private static final long serialVersionUID = -6357540199499515674L;

  private final String taxonomyName;

  private final String vocabularyName;

  public NoSuchVocabularyException(String taxonomyName, String vocabularyName) {
    super("No vocabulary exists in taxonomy '" + taxonomyName + "' with the specified name '" + vocabularyName + "'");
    this.taxonomyName = taxonomyName;
    this.vocabularyName = vocabularyName;
  }

  public String getTaxonomyName() {
    return taxonomyName;
  }

  public String getVocabularyName() {
    return vocabularyName;
  }
}
