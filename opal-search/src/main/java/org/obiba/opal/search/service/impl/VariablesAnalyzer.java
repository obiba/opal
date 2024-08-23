/*
 * Copyright (c) 2024 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.search.service.impl;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import java.util.List;

public class VariablesAnalyzer extends Analyzer {

  private final int minGram;
  private final int maxGram;

  public VariablesAnalyzer(int minGram, int maxGram) {
    this.minGram = minGram;
    this.maxGram = maxGram;
  }
  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
    final StandardTokenizer src = new StandardTokenizer();
    src.setMaxTokenLength(255);
    TokenStream tok = new LowerCaseFilter(src);
    tok = new StopFilter(tok, new CharArraySet(List.of(" ", "_", ".", ";", ":", ",", "|", "&"), true));
    tok = new NGramTokenFilter(tok, minGram, maxGram, true);
    return new TokenStreamComponents(
        r -> {
          src.setMaxTokenLength(255);
          src.setReader(r);
        },
        tok);

  }
}
