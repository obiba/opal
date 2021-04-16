/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.datashield.support;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.model.DataShield;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataShieldROptionsParser {

  private static final Logger log = LoggerFactory.getLogger(DataShieldROptionsParser.class);

  @SuppressWarnings({ "OverlyLongMethod" })
  public List<DataShield.DataShieldROptionDto> parse(String options) {
    List<DataShield.DataShieldROptionDto> optionDtos = new ArrayList<>();
    String source = options.replaceAll("[\n\r\t\f]", "");

    int length = source.length();
    boolean comma = false;
    int b = 0;
    int i = 0;
    while(i < length) {
      char c = source.charAt(i);

      switch(c) {
        case '(':
          i = skipOverParenthesis(i, source);
          break;
        case ',':
          comma = true;
          break;
      }

      if(comma) {
        try {
          optionDtos.add(buildOptionDto(source.substring(b, i)));
        } catch(IllegalArgumentException e) {
          log.warn("Datashield R option parse failed", e);
        }
        b = i + 1;
        comma = false;
      }

      i++;
    }

    if(b < i) {
      try {
        optionDtos.add(buildOptionDto(source.substring(b, i)));
      } catch(IllegalArgumentException e) {
        log.warn("Datashield R option parse failed", e);
      }
    }

    return optionDtos;
  }

  private DataShield.DataShieldROptionDto buildOptionDto(String option) {
    int pos = option.indexOf('=');
    Preconditions.checkArgument(pos != -1, "R option format is invalid: '" + option + "'");

    return DataShield.DataShieldROptionDto.newBuilder()//
        .setName(option.substring(0, pos).replaceAll(" ", ""))//
        .setValue(option.substring(pos + 1).replaceAll("^[\"|']|[\"|']$", "")).build();
  }

  private static int skipOverParenthesis(int index, CharSequence value) {
    int count = 0;
    int i = index;
    for(; i < value.length(); i++) {
      char c = value.charAt(i);
      switch(c) {
        case '(':
          count++;
          break;
        case ')':
          count--;
          break;
      }

      if(count == 0) break;
    }

    return i;
  }

}
