/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.cli.client;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

public class OpalConsoleTest {

  private final String testFileName;

  private final List<String> testCommandLines;

  public OpalConsoleTest() throws IOException {
    String fullClassName = this.getClass().getCanonicalName();
    Pattern p = Pattern.compile("\\.");
    Matcher m = p.matcher(fullClassName);
    testFileName = m.replaceAll("/") + ".txt";
    ClassPathResource classPathResource = new ClassPathResource(testFileName);
    testCommandLines = getStringsToTest(classPathResource.getFile());
  }

  @Test
  public void testParsingNoQuotes() throws Exception {
    String cmdline = testCommandLines.get(0);
    String[] args = OpalConsole.parseArguments(cmdline);
    assertThat(cmdline, args[0], is("export"));
    assertThat(cmdline, args[1], is("--destination=opal"));
    assertThat(cmdline, args[2], is("onyx.Participants"));
    assertThat(cmdline, args.length, is(3));
  }

  @Test
  public void testParsingOneSetOfDoubleQuotes() throws Exception {
    String cmdline = testCommandLines.get(1);
    String[] args = OpalConsole.parseArguments(cmdline);
    assertThat(cmdline, args[3], is("onyx.Instrument Logs"));
  }

  @Test
  public void testParsingTwoSetsOfDoubleQuotes() throws Exception {
    String cmdline = testCommandLines.get(2);
    String[] args = OpalConsole.parseArguments(cmdline);
    assertThat(cmdline, args[3], is("onyx.Instrument Logs"));
    assertThat(cmdline, args[4], is("onyx.Participant Names"));
  }

  @Test
  public void testParsingOrphanDoubleQuote() throws Exception {
    String cmdline = testCommandLines.get(3);
    String[] args = OpalConsole.parseArguments(cmdline);
    assertThat(cmdline, args[4], is("onyx.OrphanQuote"));
  }

  @Test
  public void testParsingOrphanDoubleQuoteAllAlone() throws Exception {
    String cmdline = testCommandLines.get(4);
    String[] args = OpalConsole.parseArguments(cmdline);
    assertThat(cmdline, args[3], is("onyx.Instrument Logs"));
    assertThat(cmdline, args.length, is(4));
  }

  private List<String> getStringsToTest(File aFile) {
    List<String> strings = new ArrayList<String>();

    try {
      BufferedReader input = new BufferedReader(new FileReader(aFile));
      try {
        String line = null;
        while((line = input.readLine()) != null) {
          strings.add(line);
        }
      } finally {
        input.close();
      }
    } catch(IOException ex) {
      ex.printStackTrace();
    }

    return strings;
  }
}
