/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.obiba.magma.Timestamped;
import org.obiba.magma.Value;

/**
 * Utility class for responses for {@code Timestamped} instances.
 */
public final class TimestampedResponses {

  private TimestampedResponses() {}

  /**
   * Evaluates a request against a {@code Timestamped}. This method will compare a client's timestamp against the
   * {@code lastUpdate} timestamp and respond with a 304 (Not Modified) accordingly.
   */
  public static void evaluate(Request request, Timestamped stamped) {
    Value lastModified = stamped.getTimestamps().getLastUpdate();
    if(!lastModified.isNull()) {

      // Don't compare milliseconds because HTTP headers don't have that precision
      Calendar calendar = GregorianCalendar.getInstance();
      calendar.setTime((Date) lastModified.getValue());
      calendar.clear(Calendar.MILLISECOND);

      Response.ResponseBuilder builder = request.evaluatePreconditions(calendar.getTime());
      if(builder != null) {
        // Hijack the normal flow.
        throw new UnsatisfiedPreconditionException(builder);
      }
    }
  }

  /**
   * Produces a {@code ResponseBuilder} with {@code lastModified} timestamp set.
   */
  public static Response.ResponseBuilder ok(Timestamped stamped, Object entity) {
    return ok(stamped).entity(entity);
  }

  public static Response.ResponseBuilder ok(Timestamped stamped) {
    return with(Response.ok(), stamped);
  }

  public static Response.ResponseBuilder with(Response.ResponseBuilder builder, Timestamped stamped) {
    Value lastModified = stamped.getTimestamps().getLastUpdate();
    // This null-check shouldn't be necessary: https://issues.jboss.org/browse/RESTEASY-630
    if(!lastModified.isNull()) {
      return builder.lastModified((Date) stamped.getTimestamps().getLastUpdate().getValue());
    }
    return builder;
  }

}
