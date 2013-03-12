/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Utility methods for command lines.
 */
public class CommandLines {
  //
  // Constants
  //

  private static final String WHITESPACE_AND_QUOTE = " \t\r\n\"";

  private static final String QUOTE_ONLY = "\"";

  //
  // Methods
  //

  /**
   * Parses array of arguments using spaces as delimiters. Quoted strings (including spaces) are considered a single
   * argument. For example the command line:<br>{@code export --destination=opal onyx.Participants "onyx.Instrument Logs"}<br/>
   * would yield the array:<br/>
   * a[0] = export<br/>
   * a[1] = --destination=opal<br/>
   * a[2] = onyx.Participants<br/>
   * a[3] = onyx.Instrument Logs<br/>
   *
   * @param string A command line of arguments to be parsed.
   * @return An array of arguments.
   */
  public static String[] parseArguments(String commandLine) {
    List<String> arguments = new ArrayList<String>();
    String deliminator = WHITESPACE_AND_QUOTE;
    StringTokenizer parser = new StringTokenizer(commandLine, deliminator, true);

    String token = null;
    while(parser.hasMoreTokens()) {
      token = parser.nextToken(deliminator);
      if(!token.equals(QUOTE_ONLY)) {
        if(!token.trim().equals("")) {
          arguments.add(token);
        }
      } else {
        if(deliminator.equals(WHITESPACE_AND_QUOTE)) {
          deliminator = QUOTE_ONLY;
        } else {
          deliminator = WHITESPACE_AND_QUOTE;
        }
      }
    }
    return arguments.toArray(new String[0]);
  }
}
