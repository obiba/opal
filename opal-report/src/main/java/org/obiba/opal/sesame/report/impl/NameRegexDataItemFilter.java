/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.sesame.report.impl;

import java.util.regex.Pattern;

import org.obiba.opal.elmo.concepts.DataItem;
import org.obiba.opal.sesame.report.IDataItemFilter;
import org.obiba.opal.sesame.report.ReportQueryBuilder;
import org.openrdf.elmo.sesame.SesameManager;
import org.openrdf.query.parser.sparql.SPARQLUtil;

/**
 * 
 */
public class NameRegexDataItemFilter implements IDataItemFilter {

  private String pattern;

  private Pattern compiled;

  private boolean filterOut = true;

  public NameRegexDataItemFilter() {
    filterOut = true;
  }

  public boolean accept(DataItem dataItem) {
    if (dataItem == null) {
      throw new NullPointerException("dataItem cannot be null");
    }
    boolean matches = getPattern().matcher(dataItem.getQName().getLocalPart()).matches();

    if (filterOut) {
      // Accept if the name does not match the pattern
      return !matches;
    }

    return matches;
  }

  public void contribute(ReportQueryBuilder builder, SesameManager manager) {
    StringBuilder sb = new StringBuilder().append(filterOut ? '!' : "").append("REGEX( STR(").append(builder.getVariableBindingName()).append("), \"").append(
        SPARQLUtil.encodeString(pattern)).append("\")");
    builder.withFilter(sb);
  }

  protected Pattern getPattern() {
    if (compiled == null) {
      compiled = Pattern.compile(pattern);
    }
    return compiled;
  }
}
