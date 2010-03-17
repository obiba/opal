/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datashield.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.shiro.SecurityUtils;
import org.obiba.magma.r.RSession;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

import com.thoughtworks.xstream.XStream;

/**
 *
 */
@Path("/r")
public class RResource {

  @GET
  @Path("/eval")
  @Produces("application/xml")
  public String eval(@QueryParam("cmd") String cmd) {
    try {
      System.out.println("Evaluatin " + cmd);
      return new XStream().toXML(getR().eval(cmd));
    } catch(REngineException e) {
      throw new RuntimeException(e);
    } catch(REXPMismatchException e) {
      throw new RuntimeException(e);
    }
  }

  @GET
  @Path("/attach/{table}")
  @Produces("application/xml")
  public String attach(@PathParam("table") String table) {
    try {
      getR().attach(MagmaEngineTableResolver.valueOf(table).resolveTable());
      return new XStream().toXML(true);
    } catch(REngineException e) {
      throw new RuntimeException(e);
    } catch(REXPMismatchException e) {
      throw new RuntimeException(e);
    }
  }

  private RSession getR() {
    RSession script = (RSession) SecurityUtils.getSubject().getSession().getAttribute(RSession.class);
    if(script == null) {
      SecurityUtils.getSubject().getSession().setAttribute(RSession.class, script = new RSession());
    }
    return script;
  }

}
