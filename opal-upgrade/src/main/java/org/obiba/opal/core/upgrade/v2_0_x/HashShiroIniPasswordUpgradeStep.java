/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.upgrade.v2_0_x;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.shiro.config.Ini;
import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.util.StringUtils;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.obiba.shiro.tools.hasher.Hasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharSink;
import com.google.common.io.Files;

public class HashShiroIniPasswordUpgradeStep extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(HashShiroIniPasswordUpgradeStep.class);

  private File srcIniFile;

  private File destIniFile;

  private List<String> lines;

  @Value("${OPAL_HOME}/conf/shiro.ini")
  public void setSrcIniFile(File srcIniFile) {
    this.srcIniFile = srcIniFile;
  }

  @Value("${OPAL_HOME}/conf/shiro.ini")
  public void setDestIniFile(File destIniFile) {
    this.destIniFile = destIniFile;
  }

  @Override
  public void execute(Version currentVersion) {

    try {
      lines = Files.readLines(srcIniFile, Charsets.UTF_8);

      Map<String, String> usernamePasswords = getUsernamePasswords();
      for(Map.Entry<String, String> entry : usernamePasswords.entrySet()) {
        String username = entry.getKey();
        String password = entry.getValue();

        Pattern pattern = Pattern.compile("(" + username + "\\s*=\\s*)(" + Pattern.quote(password) + ")(.*)$");
        for(String line : ImmutableList.copyOf(lines)) {
          Matcher matcher = pattern.matcher(line);
          if(matcher.find()) {
            hashAndReplacePassword(password, line, matcher);
          }
        }
      }

      backupAndWriteIniFile();

    } catch(IOException e) {
      log.error("Cannot hash password in {}. " +
              "Encrypt manually your passwords using shiro-hasher tools (included in Opal tools directory)",
          srcIniFile.getAbsolutePath(), e);
    }
  }

  private Map<String, String> getUsernamePasswords() {
    Ini ini = new Ini();
    ini.loadFromPath(srcIniFile.getAbsolutePath());
    Ini.Section section = ini.getSection(IniRealm.USERS_SECTION_NAME);
    if(section == null || section.isEmpty()) {
      return Collections.emptyMap();
    }

    Map<String, String> map = new LinkedHashMap<>();
    for(Map.Entry<String, String> entry : section.entrySet()) {
      String username = entry.getKey();
      String[] passwordAndRolesArray = StringUtils.split(entry.getValue());
      String password = passwordAndRolesArray[0];
      map.put(username, password);
    }
    return map;
  }

  private void hashAndReplacePassword(String password, String line, Matcher matcher) {
    String hash = Hasher.hash(password);
    StringBuffer sb = new StringBuffer();
    matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(1)) + Matcher.quoteReplacement(hash) +
        Matcher.quoteReplacement(matcher.group(3)));
    int index = lines.indexOf(line);
    lines.remove(index);
    lines.add(index, sb.toString());
  }

  private void backupAndWriteIniFile() throws IOException {
    Files.copy(srcIniFile, new File(srcIniFile.getAbsolutePath() + ".opal1-backup"));
    CharSink charSink = new CharSink() {
      @Override
      public Writer openStream() throws IOException {
        return new FileWriter(destIniFile);
      }
    };
    charSink.writeLines(lines);
  }

}
