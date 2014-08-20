package org.obiba.opal.web.shell;

import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.commands.ValidateCommand;
import org.obiba.opal.shell.service.CommandJobService;
import org.obiba.opal.web.model.Opal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Component
@Transactional(readOnly = true)
@Scope("request")
@Path("/validation")
public class ValidationResource {

    private static final Logger log = LoggerFactory.getLogger(ValidationResource.class);

    @Autowired
    private CommandJobService commandJobService;

    @GET
    @Path("/result/{jobId}")
    public Response getResult(@PathParam("jobId") Integer jobId) {
        CommandJob job = commandJobService.getCommand(jobId);
        String errorMsg;

        if (job != null) {
            Command cmd = job.getCommand();
            //making sure the command is of the right type
            if (cmd instanceof ValidateCommand) {
                Opal.ValidationResultDto dto = ((ValidateCommand) cmd).getResult();
                return Response.ok(dto).build();
            } else {
                errorMsg = "Job not of right type. Was " + cmd.getName();
                log.error(errorMsg);
            }
        } else {
            errorMsg = "Job not found with id " + jobId;
            log.error(errorMsg);
        }

        return Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).build();
    }
}
