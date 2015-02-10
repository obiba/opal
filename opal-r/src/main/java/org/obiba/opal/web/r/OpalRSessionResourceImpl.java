/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.r;

import java.util.List;

import javax.annotation.Nullable;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.obiba.magma.type.DateTimeType;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.r.ROperationWithResult;
import org.obiba.opal.r.StringAssignROperation;
import org.obiba.opal.r.service.OpalRSession;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.r.service.RCommand;
import org.obiba.opal.web.model.OpalR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Handles web services on a particular R session of the invoking Opal user.
 */
@Component("opalRSessionResource")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class OpalRSessionResourceImpl extends AbstractOpalRSessionResource implements OpalRSessionResource {

  @Autowired
  private OpalRSessionManager opalRSessionManager;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private IdentifiersTableService identifiersTableService;

  private OpalRSession rSession;

  @Override
  public void setOpalRSession(OpalRSession rSession) {
    this.rSession = rSession;
  }

  @Override
  public OpalR.RSessionDto getRSession() {
    return Dtos.asDto(rSession);
  }

  @Override
  public Response removeRSession() {
    opalRSessionManager.removeSubjectRSession(rSession.getId());
    return Response.ok().build();
  }

  @Override
  public Response setCurrentRSession() {
    throw new UnsupportedOperationException("Unsupported operation: please upgrade your opal R package.");
  }

  @Override
  public Response execute(String script, boolean async, String body) {
    String rScript = script;
    if(Strings.isNullOrEmpty(rScript)) {
      rScript = body;
    }
    return executeScript(rSession, rScript, async);
  }

  @Override
  public Response ls() {
    return executeScript(rSession, "base::ls()");
  }

  @Override
  public Response assign(MultivaluedMap<String, String> symbols) {
    rSession.execute(new StringAssignROperation(symbols));
    return ls();
  }

  @Override
  public RSymbolResource getRSymbolResource(String name) {
    return onGetRSymbolResource(name);
  }

  @Override
  public List<OpalR.RCommandDto> getRCommands() {
    ImmutableList.Builder<OpalR.RCommandDto> commands = ImmutableList.builder();

    commands.addAll(Iterables.transform(getOpalRSession().getRCommands(), new Function<RCommand, OpalR.RCommandDto>() {
      @Nullable
      @Override
      public OpalR.RCommandDto apply(@Nullable RCommand rCommand) {
        return asDto(rCommand);
      }
    }));

    return commands.build();
  }

  @Override
  public OpalR.RCommandDto getRCommand(@PathParam("rid") String rid, @QueryParam("wait") @DefaultValue("false") boolean wait) {
    RCommand rCommand = getOpalRSession().getRCommand(rid);
    if(!rCommand.isFinished() && wait) {
      while(!getOpalRSession().getRCommand(rid).isFinished()) {
        try {
          synchronized(rCommand) {
            rCommand.wait();
          }
        } catch(InterruptedException e) {
          return asDto(rCommand);
        }
      }
    }
    return asDto(rCommand);
  }

  @Override
  public Response removeRCommand(@PathParam("rid") String rid) {
    if(getOpalRSession().hasRCommand(rid)) {
      getOpalRSession().removeRCommand(rid);
    }
    return Response.ok().build();
  }

  @Override
  public Response getRCommandResult(@PathParam("rid") String rid,
      @QueryParam("rm") @DefaultValue("true") boolean remove, @QueryParam("wait") @DefaultValue("false") boolean wait) {
    RCommand rCommand = getOpalRSession().getRCommand(rid);
    Response resp = Response.noContent().build();
    if(!rCommand.isFinished()) {
      if(wait) {
        while(!getOpalRSession().getRCommand(rid).isFinished()) {
          try {
            synchronized(rCommand) {
              rCommand.wait();
            }
          } catch(InterruptedException e) {
            return resp;
          }
        }
      } else {
        return resp;
      }
    }

    return getFinishedRCommandResult(rCommand, remove);
  }

  private Response getFinishedRCommandResult(RCommand rCommand, boolean remove) {
    Response resp = Response.noContent().build();
    if(rCommand.hasResult()) {
      ROperationWithResult rop = rCommand.asROperationWithResult();
      if(rop.hasRawResult()) {
        resp = Response.ok().entity(rop.getRawResult().asBytes()).build();
      }
    }
    if(remove) getOpalRSession().removeRCommand(rCommand.getId());
    return resp;
  }

  private OpalR.RCommandDto asDto(RCommand rCommand) {
    OpalR.RCommandDto.Builder builder = OpalR.RCommandDto.newBuilder().setId(rCommand.getId());
    builder.setScript(rCommand.toString());
    builder.setCreateDate(DateTimeType.get().valueOf(rCommand.getCreateDate()).toString());
    builder.setStatus(rCommand.getStatus().name());
    builder.setWithResult(rCommand.hasResult());
    if(rCommand.getStatus() != RCommand.Status.PENDING) {
      builder.setStartDate(DateTimeType.get().valueOf(rCommand.getStartDate()).toString());
    }
    if(rCommand.getStatus() == RCommand.Status.COMPLETED) {
      builder.setEndDate(DateTimeType.get().valueOf(rCommand.getEndDate()).toString());
    }
    if(rCommand.getStatus() == RCommand.Status.FAILED) {
      builder.setEndDate(DateTimeType.get().valueOf(rCommand.getEndDate()).toString());
      if(rCommand.hasError()) builder.setError(rCommand.getError());
    }
    return builder.build();
  }

  protected RSymbolResource onGetRSymbolResource(String name) {
    SecuredRSymbolResource resource = applicationContext
        .getBean("securedRSymbolResource", SecuredRSymbolResource.class);
    resource.setName(name);
    resource.setOpalRSession(rSession);
    resource.setIdentifiersTableService(identifiersTableService);
    return resource;
  }

  protected OpalRSession getOpalRSession() {
    return rSession;
  }

}
