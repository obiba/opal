/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.r;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.obiba.magma.type.DateTimeType;
import org.obiba.opal.core.service.DataExportService;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.core.service.ResourceReferenceService;
import org.obiba.opal.r.StringAssignROperation;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.r.service.RCacheHelper;
import org.obiba.opal.r.service.RServerSession;
import org.obiba.opal.spi.r.RCommand;
import org.obiba.opal.spi.r.ROperationWithResult;
import org.obiba.opal.spi.r.RSerialize;
import org.obiba.opal.spi.r.RServerResult;
import org.obiba.opal.web.model.OpalR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import jakarta.annotation.Nullable;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * Base class for handling current R session related web services.
 */
public abstract class AbstractRSessionResource implements RSessionResource {

  private static final Logger log = LoggerFactory.getLogger(AbstractRSessionResource.class);

  @Autowired
  private OpalRSessionManager opalRSessionManager;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private IdentifiersTableService identifiersTableService;

  @Autowired
  private DataExportService dataExportService;

  @Autowired
  private RCacheHelper rCacheHelper;

  @Autowired
  private ResourceReferenceService resourceReferenceService;

  private RServerSession rSession;

  @Override
  public void setRServerSession(RServerSession rSession) {
    if (rSession.getExecutionContext().equals("DataSHIELD") && !getExecutionContext().equals(rSession.getExecutionContext()))
      throw new BadRequestException(String.format("Not a valid execution context '%s', expecting '%s'", rSession.getExecutionContext(), getExecutionContext()));
    this.rSession = rSession;
  }

  @Override
  public OpalR.RSessionDto getRSession() {
    return Dtos.asDto(rSession);
  }

  @Override
  public Response removeRSession(String saveId) {
    if (!Strings.isNullOrEmpty(saveId)) {
      opalRSessionManager.saveSubjectRSession(rSession.getId(), saveId);
    }
    opalRSessionManager.removeSubjectRSession(rSession.getId());
    return Response.ok().build();
  }

  @Override
  public Response saveWorkspace(String saveId) {
    if (!Strings.isNullOrEmpty(saveId)) {
      opalRSessionManager.saveSubjectRSession(rSession.getId(), saveId);
    }
    return Response.status(Strings.isNullOrEmpty(saveId) ? Response.Status.BAD_REQUEST : Response.Status.OK).build();
  }

  @Override
  public Response restoreWorkspace(String workspaceId) {
    if (!Strings.isNullOrEmpty(workspaceId)) {
      opalRSessionManager.restoreSubjectRSession(rSession.getId(), workspaceId);
    }
    return Response.status(Strings.isNullOrEmpty(workspaceId) ? Response.Status.BAD_REQUEST : Response.Status.OK).build();
  }

  @Override
  public Response setCurrentRSession() {
    throw new UnsupportedOperationException("Unsupported operation: please upgrade your opal R package.");
  }

  @Override
  public Response lsBinary() {
    return RSessionResourceHelper.executeScript(rSession, "base::ls()", RSerialize.RAW);
  }

  @Override
  public Response lsJSON() {
    return RSessionResourceHelper.executeScript(rSession, "base::ls()", RSerialize.JSON);
  }

  @Override
  public Response assign(MultivaluedMap<String, String> symbols) {
    rSession.execute(new StringAssignROperation(symbols));
    return lsBinary();
  }

  @Override
  public RSymbolResource getRSymbolResource(String name) {
    return onGetRSymbolResource(name);
  }

  @Override
  public List<OpalR.RCommandDto> getRCommands() {
    ImmutableList.Builder<OpalR.RCommandDto> commands = ImmutableList.builder();

    commands.addAll(Iterables.transform(getRServerSession().getRCommands(), new Function<RCommand, OpalR.RCommandDto>() {
      @Nullable
      @Override
      public OpalR.RCommandDto apply(@Nullable RCommand rCommand) {
        return asDto(rCommand);
      }
    }));

    return commands.build();
  }

  @Override
  public OpalR.RCommandDto getRCommand(String rid, boolean wait) {
    RCommand rCommand = getRServerSession().getRCommand(rid);
    if (!rCommand.isFinished() && wait) {
      while (!getRServerSession().getRCommand(rid).isFinished()) {
        try {
          synchronized (rCommand) {
            rCommand.wait();
          }
        } catch (InterruptedException e) {
          return asDto(rCommand);
        }
      }
    }
    return asDto(rCommand);
  }

  @Override
  public Response removeRCommand(String rid) {
    if (getRServerSession().hasRCommand(rid)) {
      getRServerSession().removeRCommand(rid);
    }
    return Response.ok().build();
  }

  @Override
  public Response getRCommandResultRaw(String rid, boolean remove, boolean wait) {
    return getRCommandResult(rid, remove, wait, true);
  }

    @Override
  public Response getRCommandResultJSON(String rid, boolean remove, boolean wait) {
      return getRCommandResult(rid, remove, wait, false);
  }

  private Response getRCommandResult(String rid, boolean remove, boolean wait, boolean serialize) {
    RCommand rCommand = getRServerSession().getRCommand(rid);
    Response resp = Response.noContent().build();
    if (!rCommand.isFinished()) {
      if (wait) {
        while (!getRServerSession().getRCommand(rid).isFinished()) {
          try {
            synchronized (rCommand) {
              rCommand.wait();
            }
          } catch (InterruptedException e) {
            return resp;
          }
        }
      } else {
        return resp;
      }
    }

    return getFinishedRCommandResult(rCommand, remove, serialize);
  }

  private Response getFinishedRCommandResult(RCommand rCommand, boolean remove, boolean serialize) {
    Response resp = Response.noContent().build();
    if (rCommand.hasResult()) {
      ROperationWithResult rop = rCommand.asROperationWithResult();
      if (rop.hasResult()) {
        if (rop.getResult().isRaw())
          resp = Response.ok().entity(rop.getResult().asBytes()).type(MediaType.APPLICATION_OCTET_STREAM).build();
        else
          resp = Response.ok().entity(rop.getResult().asJSON()).type(MediaType.APPLICATION_JSON).build();
      }
    }
    if (remove) getRServerSession().removeRCommand(rCommand.getId());
    return resp;
  }

  private OpalR.RCommandDto asDto(RCommand rCommand) {
    OpalR.RCommandDto.Builder builder = OpalR.RCommandDto.newBuilder().setId(rCommand.getId());
    builder.setScript(rCommand.toString());
    builder.setCreateDate(DateTimeType.get().valueOf(rCommand.getCreateDate()).toString());
    builder.setStatus(rCommand.getStatus().name());
    builder.setWithResult(rCommand.hasResult());
    if (rCommand.getStatus() != RCommand.Status.PENDING) {
      builder.setStartDate(DateTimeType.get().valueOf(rCommand.getStartDate()).toString());
    }
    if (rCommand.getStatus() == RCommand.Status.COMPLETED) {
      builder.setEndDate(DateTimeType.get().valueOf(rCommand.getEndDate()).toString());
    }
    if (rCommand.getStatus() == RCommand.Status.FAILED) {
      builder.setEndDate(DateTimeType.get().valueOf(rCommand.getEndDate()).toString());
      if (rCommand.hasError()) builder.setError(rCommand.getError());
    }
    return builder.build();
  }

  protected RSymbolResource onGetRSymbolResource(String name) {
    OpalRSymbolResource resource = applicationContext
        .getBean("opalRSymbolResource", OpalRSymbolResource.class);
    resource.setName(name);
    resource.setRServerSession(rSession);
    resource.setIdentifiersTableService(identifiersTableService);
    resource.setDataExportService(dataExportService);
    resource.setRCacheHelper(rCacheHelper);
    resource.setResourceReferenceService(resourceReferenceService);
    return resource;
  }

  protected RServerSession getRServerSession() {
    return rSession;
  }

  protected ResourceReferenceService getResourceReferenceService() {
    return resourceReferenceService;
  }

  protected abstract String getExecutionContext();
}
