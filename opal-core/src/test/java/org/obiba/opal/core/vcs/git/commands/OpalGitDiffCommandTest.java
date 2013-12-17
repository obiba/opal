/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.vcs.git.commands;

import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.obiba.opal.core.vcs.git.OpalGitException;

import static org.hamcrest.MatcherAssert.assertThat;

public class OpalGitDiffCommandTest extends AbstractOpalGitCommandTest {

  private static final String DIFF_VARIABLE = "diff --git a/TestView/TOTO_VAR.js b/TestView/TOTO_VAR.js\n" +
      "new file mode 100644\n" +
      "index 0000000..ec747fa\n" +
      "--- /dev/null\n" +
      "+++ b/TestView/TOTO_VAR.js\n" +
      "@@ -0,0 +1 @@\n" +
      "+null\n" +
      "\\ No newline at end of file\n";

  private static final String DIFF_VIEW = "diff --git a/TestView/View.xml b/TestView/View.xml\n" +
      "index 59826e4..9ba271d 100644\n" +
      "--- a/TestView/View.xml\n" +
      "+++ b/TestView/View.xml\n" +
      "@@ -87,8 +87,13 @@\n" +
      "           <attribute name=\"script\" valueType=\"text\">$(&apos;COORDINATE&apos;)</attribute>\n" +
      "         </attributes>\n" +
      "       </variable>\n" +
      "+      <variable name=\"TOTO_VAR\" valueType=\"integer\" entityType=\"PostalCode\" unit=\"\" mimeType=\"\">\n" +
      "+        <attributes>\n" +
      "+          <attribute name=\"script\" valueType=\"text\">null</attribute>\n" +
      "+        </attributes>\n" +
      "+      </variable>\n" +
      "     </variables>\n" +
      "   </variables>\n" +
      "   <created valueType=\"datetime\">2013-09-17T16:09:00.773-0400</created>\n" +
      "-  <updated valueType=\"datetime\">2013-09-19T11:20:52.956-0400</updated>\n" +
      "+  <updated valueType=\"datetime\">2013-09-19T11:48:01.742-0400</updated>\n" +
      " </org.obiba.magma.views.View>\n" +
      "\\ No newline at end of file\n";

  private static final String DIFF_VIEW_TWO_VERSIONS_BACK = "diff --git a/TestView/View.xml b/TestView/View.xml\n" +
      "index ba4eb49..9ba271d 100644\n" +
      "--- a/TestView/View.xml\n" +
      "+++ b/TestView/View.xml\n" +
      "@@ -87,8 +87,13 @@\n" +
      "           <attribute name=\"script\" valueType=\"text\">$(&apos;COORDINATE&apos;)</attribute>\n" +
      "         </attributes>\n" +
      "       </variable>\n" +
      "+      <variable name=\"TOTO_VAR\" valueType=\"integer\" entityType=\"PostalCode\" unit=\"\" mimeType=\"\">\n" +
      "+        <attributes>\n" +
      "+          <attribute name=\"script\" valueType=\"text\">null</attribute>\n" +
      "+        </attributes>\n" +
      "+      </variable>\n" +
      "     </variables>\n" +
      "   </variables>\n" +
      "   <created valueType=\"datetime\">2013-09-17T16:09:00.773-0400</created>\n" +
      "-  <updated valueType=\"datetime\">2013-09-19T11:18:57.428-0400</updated>\n" +
      "+  <updated valueType=\"datetime\">2013-09-19T11:48:01.742-0400</updated>\n" +
      " </org.obiba.magma.views.View>\n" +
      "\\ No newline at end of file\n";

  private static final String DIFF_VIEW_WITH_HEAD = "diff --git a/TestView/View.xml b/TestView/View.xml\n" +
      "index 8965792..9ba271d 100644\n" +
      "--- a/TestView/View.xml\n" +
      "+++ b/TestView/View.xml\n" +
      "@@ -14,8 +14,7 @@\n" +
      "           <attribute name=\"label\" valueType=\"text\" locale=\"en\">Place name</attribute>\n" +
      "           <attribute name=\"index\" valueType=\"text\">0</attribute>\n" +
      "           <attribute name=\"derivedFrom\" namespace=\"opal\" valueType=\"text\">/datasource/opal-data2/table/CA/variable/PLACE_NAME</attribute>\n" +
      "-          <attribute name=\"script\" valueType=\"text\">$(&apos;PLACE_NAME&apos;)\n" +
      "-$(&apos;PLACE_NAME&apos;)</attribute>\n" +
      "+          <attribute name=\"script\" valueType=\"text\">$(&apos;PLACE_NAME&apos;)</attribute>\n" +
      "         </attributes>\n" +
      "       </variable>\n" +
      "       <variable name=\"STATE\" valueType=\"text\" entityType=\"PostalCode\" unit=\"\" mimeType=\"\">\n" +
      "@@ -88,8 +87,13 @@\n" +
      "           <attribute name=\"script\" valueType=\"text\">$(&apos;COORDINATE&apos;)</attribute>\n" +
      "         </attributes>\n" +
      "       </variable>\n" +
      "+      <variable name=\"TOTO_VAR\" valueType=\"integer\" entityType=\"PostalCode\" unit=\"\" mimeType=\"\">\n" +
      "+        <attributes>\n" +
      "+          <attribute name=\"script\" valueType=\"text\">null</attribute>\n" +
      "+        </attributes>\n" +
      "+      </variable>\n" +
      "     </variables>\n" +
      "   </variables>\n" +
      "   <created valueType=\"datetime\">2013-09-17T16:09:00.773-0400</created>\n" +
      "-  <updated valueType=\"datetime\">2013-09-19T11:00:19.008-0400</updated>\n" +
      "+  <updated valueType=\"datetime\">2013-09-19T11:48:01.742-0400</updated>\n" +
      " </org.obiba.magma.views.View>\n" +
      "\\ No newline at end of file\n";

  @Test
  public void testDiffWithValidCommit() {
    OpalGitDiffCommand command = new OpalGitDiffCommand.Builder(versionControlSystem.getRepository(DATASOURCE_NAME),
        COMMIT_ID).addDatasourceName(DATASOURCE_NAME).build();
    List<String> diffs = command.execute();
    assertThat(diffs, matches(DIFF_VARIABLE));
    assertThat(diffs, matches(DIFF_VIEW));
  }

  @Test
  public void testDiffWithValidVariablePath() {
    OpalGitDiffCommand command = new OpalGitDiffCommand.Builder(versionControlSystem.getRepository(DATASOURCE_NAME),
        COMMIT_ID).addPath("TestView/TOTO_VAR.js").addDatasourceName(DATASOURCE_NAME).build();
    List<String> diffs = command.execute();
    assertThat(diffs, matches(DIFF_VARIABLE));
  }

  @Test(expected = OpalGitException.class)
  public void testDiffWithInvalidCommit() {
    new OpalGitDiffCommand.Builder(versionControlSystem.getRepository(DATASOURCE_NAME), BAD_COMMIT_ID)
        .addPath("TestView").addDatasourceName(DATASOURCE_NAME).build().execute();
  }

  @Test
  public void testDiffWithValidViewPath() {
    OpalGitDiffCommand command = new OpalGitDiffCommand.Builder(versionControlSystem.getRepository(DATASOURCE_NAME),
        COMMIT_ID).addPath("TestView").addDatasourceName(DATASOURCE_NAME).build();
    List<String> diffs = command.execute();
    assertThat(diffs, matches(DIFF_VARIABLE));
    assertThat(diffs, matches(DIFF_VIEW));
  }

  @Test
  public void testDiffWithSelf() {
    new OpalGitDiffCommand.Builder(versionControlSystem.getRepository(DATASOURCE_NAME), COMMIT_ID)
        .addDatasourceName(DATASOURCE_NAME).addNthCommit(0).build().execute();
  }

  @Test
  public void testDiffWithTwoVersionsBack() {
    OpalGitDiffCommand command = new OpalGitDiffCommand.Builder(versionControlSystem.getRepository(DATASOURCE_NAME),
        COMMIT_ID).addPath("TestView").addDatasourceName(DATASOURCE_NAME).addNthCommit(2).build();
    List<String> diffs = command.execute();
    assertThat(diffs, matches(DIFF_VIEW_TWO_VERSIONS_BACK));
  }

  @Test
  public void testDiffWithCurrent() {
    OpalGitDiffCommand command = new OpalGitDiffCommand.Builder(versionControlSystem.getRepository(DATASOURCE_NAME),
        "HEAD").addPath("TestView/View.xml").addDatasourceName(DATASOURCE_NAME)
        .addPreviousCommitId("be77432d15dec81b4c60ed858d5d678ceb247171").build();
    List<String> diffs = command.execute();
    assertThat(diffs, matches(DIFF_VIEW_WITH_HEAD));
  }

  @Test
  public void testDiffWithCurrentUsingGitVCS() {
    List<String> diffs = versionControlSystem
        .getDiffEntries(DATASOURCE_NAME, "HEAD", "be77432d15dec81b4c60ed858d5d678ceb247171", "TestView/View.xml");
    assertThat(diffs, matches(DIFF_VIEW_WITH_HEAD));
  }

  /**
   * Matcher class for diff contents
   *
   * @param expected
   * @return
   */
  private static Matcher<List<String>> matches(final String expected) {

    return new BaseMatcher<List<String>>() {

      @Override
      @SuppressWarnings("unchecked")
      public boolean matches(Object diffObject) {
        return diffObject instanceof List ? matchDiffs((Iterable<String>) diffObject) : diffObject.equals(expected);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(expected);
      }

      private boolean matchDiffs(Iterable<String> diffs) {
        for(String diff : diffs) {
          if(diff.equals(expected)) return true;
        }
        return false;
      }

    };
  }
}

