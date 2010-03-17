/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datashield;

import java.io.IOException;

import org.obiba.core.util.StreamUtil;
import org.obiba.magma.r.RSession;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Cookie;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;

import com.thoughtworks.xstream.XStream;

/**
 *
 */
public class Client {

  public static void main(String[] args) throws ResourceException, IOException, REngineException, REXPMismatchException {
    RConnection connection = new RConnection();

    ClientResource client = new ClientResource("http://localhost:8182/cag");
    client.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "administrator", "password"));
    try {
      attach(client, "opal-data.Impedance418");
      eval(client, "attach(Impedance418)");
      REXP summary = eval(client, "summary(lm(RES_BODY_IMPEDANCE ~ INPUT_PARTICIPANT_AGE + INPUT_PARTICIPANT_GENDER))");

      System.out.println("Sending to R");
      connection.parseAndEval("numstudies<-2");
      connection.parseAndEval("numpara<-3");
      connection.parseAndEval("study.coeffs<-list(rep(matrix(NA,3,4),numstudies))");

      connection.assign("coeffs1", summary.asList().at("coefficients"));
      connection.assign("coeffs2", summary.asList().at("coefficients"));
      connection.parseAndEval("study.coeffs<-list(coeffs1, coeffs2)");
      connection.parseAndEval("print(study.coeffs[[1]])");

      connection.parseAndEval(readScript());
    } finally {
      try {
        connection.close();
      } catch(Exception e) {
      }
      System.out.println("Logging out");
      ClientResource logout = new ClientResource(client);
      logout.getReference().addSegment("logout");
      logout.get();
    }
  }

  public static REXP attach(ClientResource client, String table) throws ResourceException, IOException {
    ClientResource eval = new ClientResource(client);
    eval.getReference().addSegment("r").addSegment("attach").addSegment(table);
    eval.get();
    client.getCookies().add(new Cookie("AUTH", eval.getCookieSettings().getFirstValue("AUTH")));
    return null;
  }

  public static REXP eval(ClientResource client, String cmd) throws ResourceException, IOException, REXPMismatchException {
    ClientResource eval = new ClientResource(client);
    eval.getReference().addSegment("r").addSegment("eval").addQueryParameter("cmd", cmd);
    REXP rexp = (REXP) new XStream().fromXML(eval.get().getStream());
    System.out.println(RSession.toString(rexp));
    return rexp;
  }

  public static String readScript() {
    try {
      StringBuilder sb = new StringBuilder();
      for(String line : StreamUtil.readLines(Client.class.getResourceAsStream("script.r"))) {
        sb.append(line).append('\n');
      }
      return sb.toString();
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }
}
