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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class AnalyzerFactory {

  public static Analyzer newVariablesAnalyzer() {
    return new VariablesAnalyzer(3, 3);
  }

  public static Analyzer newTablesAnalyzer() {
    return new VariablesAnalyzer(3, 3);
  }

  public static Analyzer newEntitiesAnalyzer() {
    return new StandardAnalyzer();
  }
}
