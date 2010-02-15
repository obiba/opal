/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.cli.client.command.options;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;

/**
 * Interacts with the user at the {@link Console} command line to build a list of attributes needed to construct a
 * {@link Certificate}.
 */
public class CertificateInfo {

  private final static String DEFAULT_VALUE = "Unknown";

  private List<Attribute> infoList;

  public CertificateInfo() {
    infoList = new ArrayList<Attribute>();
    infoList.add(new Attribute("CN", DEFAULT_VALUE, "What is your first and last name?"));
    infoList.add(new Attribute("OU", DEFAULT_VALUE, "What is the name of your organizational unit?"));
    infoList.add(new Attribute("O", DEFAULT_VALUE, "What is the name of your organization?"));
    infoList.add(new Attribute("L", DEFAULT_VALUE, "What is the name of your City or Locality?"));
    infoList.add(new Attribute("ST", DEFAULT_VALUE, "What is the name of your State or Province?"));
    infoList.add(new Attribute("C", DEFAULT_VALUE, "What is the two-letter country code for this unit?"));
  }

  private String getConfirmationQuestion() {
    return new StringBuilder().append("Is ").append(getDnString()).append(" correct?").toString();
  }

  private String getDnString() {
    StringBuilder sb = new StringBuilder();
    for(Attribute attribute : infoList) {
      sb.append(attribute.getName()).append("=").append(attribute.getValue()).append(", ");
    }
    sb.setLength(sb.length() - 2);
    return sb.toString();
  }

  private String getConfirmationPrompt(String answer) {
    return new StringBuilder().append("  [").append(answer).append("]:  ").toString();
  }

  /**
   * Builds Certificate attributes interactively with user.
   * @return the attributes as a String (e.g. CN=Administrator, OU=Bioinformatics, O=GQ, L=Montreal, ST=Quebec, C=CA)
   */
  public String getCertificateInfoAsString() {
    do {
      getCertificateInfoAttributes();
    } while(!isCertificateCorrect());
    return getDnString();
  }

  private void getCertificateInfoAttributes() {
    System.console().printf("%s:\n", "Certificate creation");
    for(CertificateInfo.Attribute attribute : infoList) {
      System.console().printf(" %s\n", attribute.getQuestion());
      attribute.setValue(System.console().readLine("%s", attribute.getPrompt()));
    }
  }

  private boolean isCertificateCorrect() {
    System.console().printf(" %s\n", getConfirmationQuestion());
    String ans = "no";
    do {
      String answer = System.console().readLine("%s", getConfirmationPrompt(ans));
      if(answer != null && !answer.equals("")) {
        ans = answer;
      }
    } while(!(ans.equalsIgnoreCase("yes") || ans.equalsIgnoreCase("no") || ans.equalsIgnoreCase("y") || ans.equalsIgnoreCase("n")));
    if(ans.equalsIgnoreCase("yes") || ans.equalsIgnoreCase("y")) {
      return true;
    }
    return false;
  }

  public class Attribute {

    private final String name;

    private String value;

    private final String question;

    public Attribute(String name, String value, String question) {
      this.name = name;
      this.value = value;
      this.question = question;
    }

    public String getQuestion() {
      return question;
    }

    public String getValue() {
      return value;
    }

    public String getName() {
      return name;
    }

    public void setValue(String value) {
      if(value != null && !value.equals("")) {
        this.value = value;
      }
    }

    public String getPrompt() {
      return new StringBuilder().append("  [").append(value).append("]:  ").toString();
    }

  }
}
