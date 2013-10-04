/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.vcs.git;

import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;
import org.obiba.opal.core.vcs.OpalGitException;
import org.obiba.opal.core.vcs.git.commands.OpalGitDiffCommand;
import org.obiba.opal.core.vcs.git.support.TestOpalGitVersionControlSystem;

import static org.hamcrest.MatcherAssert.assertThat;

public class OpalGitDiffCommandTest {

  private static final String COMMIT_ID = "448b81ed146cc76751c3b10b89e80cc99da63427";

  private static final String BAD_COMMIT_ID = "DeadBeefDeadBeefDeadBeefDeadBeefDeadBeef";

  private static final String DATASOURCE_NAME = "opal-data2";

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

  private static final TestOpalGitVersionControlSystem vcs = new TestOpalGitVersionControlSystem();

  @Test
  public void testBlobRetrievalWithValidCommit() {
    try {
      OpalGitDiffCommand command = new OpalGitDiffCommand.Builder(vcs.getRepository(DATASOURCE_NAME), COMMIT_ID)
          .addDatasourceName(DATASOURCE_NAME).build();
      List<String> diffs = command.execute();
      assertThat(diffs, matches(DIFF_VARIABLE));
      assertThat(diffs, matches(DIFF_VIEW));
    } catch(Exception e) {
      Assert.fail();
    }
  }

  @Test
  public void testBlobRetrievalWithValidVariablePath() {
    try {
      OpalGitDiffCommand command = new OpalGitDiffCommand.Builder(vcs.getRepository(DATASOURCE_NAME), COMMIT_ID)
          .addPath("TestView/TOTO_VAR.js").addDatasourceName(DATASOURCE_NAME).build();
      List<String> diffs = command.execute();
      assertThat(diffs, matches(DIFF_VARIABLE));
    } catch(Exception e) {
      Assert.fail();
    }
  }

  @Test(expected = OpalGitException.class)
  public void testBlobRetrievalWithInvalidCommit() {
    new OpalGitDiffCommand.Builder(vcs.getRepository(DATASOURCE_NAME), BAD_COMMIT_ID).addPath("TestView")
        .addDatasourceName(DATASOURCE_NAME).build().execute();
  }

  @Test
  public void testBlobRetrievalWithValidViewPath() {
    try {
      OpalGitDiffCommand command = new OpalGitDiffCommand.Builder(vcs.getRepository(DATASOURCE_NAME), COMMIT_ID)
          .addPath("TestView").addDatasourceName(DATASOURCE_NAME).build();
      List<String> diffs = command.execute();
      assertThat(diffs, matches(DIFF_VARIABLE));
      assertThat(diffs, matches(DIFF_VIEW));
    } catch(Exception e) {
      Assert.fail();
    }
  }

  @Test
  public void testBlobRetrievalWithSelf() {
    try {
      new OpalGitDiffCommand.Builder(vcs.getRepository(DATASOURCE_NAME), COMMIT_ID).addDatasourceName(DATASOURCE_NAME)
          .addNthCommit(0).build().execute();
    } catch(Exception e) {
      Assert.fail();
    }
  }

  @Test
  public void testBlobRetrievalWithTwoVersionsBack() {
    try {
      OpalGitDiffCommand command = new OpalGitDiffCommand.Builder(vcs.getRepository(DATASOURCE_NAME), COMMIT_ID)
          .addPath("TestView").addDatasourceName(DATASOURCE_NAME).addNthCommit(2).build();
      List<String> diffs = command.execute();
      assertThat(diffs, matches(DIFF_VIEW_TWO_VERSIONS_BACK));
    } catch(Exception e) {
      Assert.fail();
    }
  }

  /**
   * Matcher class for diff contents
   *
   * @param expected
   * @return
   */
  private static Matcher matches(final String expected) {

    return new BaseMatcher() {

      private String theExpected = expected;

      @Override
      public boolean matches(Object diffObject) {
        if(diffObject instanceof List) {
          return matchDiffs((List<String>) diffObject);
        }

        return diffObject.equals(theExpected);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(theExpected);
      }

      private boolean matchDiffs(List<String> diffs) {
        for(String diff : diffs) {
          if(diff.equals(theExpected)) return true;
        }

        return false;
      }

    };
  }
}

