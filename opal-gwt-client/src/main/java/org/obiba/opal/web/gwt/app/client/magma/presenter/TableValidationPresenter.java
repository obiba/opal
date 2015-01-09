/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.presenter;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.CommandStateDto;
import org.obiba.opal.web.model.client.opal.ValidateCommandOptionsDto;
import org.obiba.opal.web.model.client.opal.ValidationResultDto;

public class TableValidationPresenter
    extends PresenterWidget<TableValidationPresenter.Display>
    implements TableValidationUiHandlers {

  private TableDto originalTable;

    @Inject
  public TableValidationPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);

    getView().setUiHandlers(this);
  }

  public void setTable(final TableDto table) {
    originalTable = table;

    getView().setTable(table);
  }

  @Override
  protected void onBind() {
    super.onBind();
  }

  public interface Display extends View, HasUiHandlers<TableValidationUiHandlers> {

    void setValidationResult(ValidationResultDto dto);

    void clearErrorMessages();

    void setErrorMessage(String title, String msg);

    void setTable(TableDto table);

    void setValidationInProgress();

    void setValidationFinished();
  }

    private ValidateCommandOptionsDto createValidateOptions() {
        ValidateCommandOptionsDto dto = ValidateCommandOptionsDto.create();
        dto.setProject(originalTable.getDatasourceName());
        dto.setTable(originalTable.getName());
        return dto;
    }

    @Override
    public void onValidate() {
        getView().clearErrorMessages(); //clear error messages
        getView().setValidationInProgress();
        sendValidateCommandRequest();
    }

    /**
     * Handler of job validation dto.
     * Based on the job status, this will trigger one of:
     * -validation results retrieval (if finished with success)
     * -retry job state retrieval (if in progress)
     * -error message (omn any other status)
     * @param jobDto validation
     */
    private void handleValidationJobState(CommandStateDto jobDto) {
        String status = jobDto.getStatus();
        final String jobId = String.valueOf(jobDto.getId());
        if ("SUCCEEDED".equals(status)) {
            //retrieve validation result
            sendValidationResultRequest(jobId);
        } else if ("IN_PROGRESS".equals(status)) {
            Timer timer = new Timer() {
                @Override
                public void run() {
                    sendValidationJobStateRequest(jobId);
                }
            };
            timer.schedule(1000); //retry job state in 1000 millis
        } else {
            getView().clearErrorMessages();
            getView().setErrorMessage("Unexpected validation job status", status);
            getView().setValidationFinished();
        }
    }

    private void sendValidateCommandRequest() {
        String body = ValidateCommandOptionsDto.stringify(createValidateOptions());
        ResourceRequestBuilderFactory.<CommandStateDto>newBuilder().forResource(
                UriBuilders.PROJECT_COMMANDS_VALIDATE.create().build(originalTable.getDatasourceName()))
                .post()
                .withResourceBody(body)
                .withCallback(Response.SC_CREATED, new ValidateJobCallBack())
                .send();
    }

    private void sendValidationJobStateRequest(String jobId) {
        ResourceRequestBuilderFactory.<CommandStateDto>newBuilder().forResource(
                UriBuilders.SHELL_COMMAND.create().build(jobId))
                .get()
                .withCallback(new ValidateJobStateCallback())
                .send();
    }

    private void sendValidationResultRequest(String jobId) {
        ResourceRequestBuilderFactory.<ValidationResultDto>newBuilder().forResource(
                UriBuilders.VALIDATION_RESULT.create().build(jobId))
                .withCallback(new ValidationResultsCallBack()).get().send();
    }

    private void handleValidationResult(ValidationResultDto dto) {
        if (!originalTable.getName().equals(dto.getTable()) ||
                !originalTable.getDatasourceName().equals(dto.getDatasource())) {
            return; //ignore results for other tables
        }
        getView().setValidationResult(dto);
        getView().setValidationFinished();
    }

    private class ValidationResultsCallBack implements ResourceCallback<ValidationResultDto> {
        @Override
        public void onResource(Response response, ValidationResultDto resource) {
            handleValidationResult(resource);
        }
    }

    private class ValidateJobCallBack implements ResponseCodeCallback {
        @Override
        public void onResponseCode(Request request, Response response) {
            String location = response.getHeader("Location");
            String jobId = location.substring(location.lastIndexOf('/') + 1);
            sendValidationJobStateRequest(jobId);
        }
    }

    private class ValidateJobStateCallback implements ResourceCallback<CommandStateDto> {
        @Override
        public void onResource(Response response, CommandStateDto resource) {
            handleValidationJobState(resource);
        }
    }
}
