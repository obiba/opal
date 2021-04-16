/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.validator;

import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.rest.client.event.RequestErrorEvent;
import org.obiba.opal.web.gwt.validation.client.ValidationMessageResolver;
import org.obiba.opal.web.model.client.ws.ConstraintViolationErrorDto;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.base.AlertBase;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;

public class ConstrainedModal {

  private static final ValidationMessageResolver validationMessageResolver = new ValidationMessageResolver();

  private final Modal modal;

  private final Map<String, ConstrainedWidget> constrainedWidgets = Maps.newHashMap();

  public ConstrainedModal(@NotNull Modal modal) {
    this.modal = modal;
    modal.addHandler(new ModalRequestErrorHandler(), RequestErrorEvent.getType());
  }

  public void registerWidget(@NotNull String propertyPath, @NotNull String propertyLabel) {
    registerWidget(propertyPath, propertyLabel, null);
  }

  public void registerWidget(@NotNull String propertyPath, @NotNull String propertyLabel,
      @Nullable ControlGroup group) {
    constrainedWidgets.put(propertyPath, new ConstrainedWidget(propertyPath, propertyLabel, group));
  }

  private static class ConstrainedWidget {

    @NotNull
    private final String propertyPath;

    @NotNull
    private final String propertyLabel;

    @Nullable
    private final ControlGroup group;

    private ConstrainedWidget(@NotNull String propertyPath, @NotNull String propertyLabel,
        @Nullable ControlGroup group) {
      this.propertyPath = propertyPath;
      this.propertyLabel = propertyLabel;
      this.group = group;
    }

    @Nullable
    private ControlGroup getGroup() {
      return group;
    }

    @NotNull
    private String getPropertyPath() {
      return propertyPath;
    }

    @NotNull
    private String getPropertyLabel() {
      return propertyLabel;
    }
  }

  private class ModalRequestErrorHandler implements RequestErrorEvent.RequestErrorHandler {

    @Override
    public void onRequestError(RequestErrorEvent event) {
      if(event.getViolations() != null) {
        showConstraintViolations(event.getViolations());
      } else if(!Strings.isNullOrEmpty(event.getMessage())) {
        modal.addAlert(event.getMessage(), AlertType.ERROR);
      } else {
        //noinspection ThrowableResultOfMethodCallIgnored
        modal.addAlert(event.getException().getMessage(), AlertType.ERROR);
      }
    }

    private void showConstraintViolations(Iterable<ConstraintViolationErrorDto> violations) {
      for(ConstraintViolationErrorDto violation : violations) {
        String propertyPath = violation.getPropertyPath();
        String messageTemplate = violation.getMessageTemplate();
        ConstrainedWidget constrainedWidget = constrainedWidgets.get(propertyPath);
        if(constrainedWidget == null) {
          // this error is not attached to a widget
          modal.addAlert(propertyPath + " " + validationMessageResolver.get(messageTemplate), AlertType.ERROR);
        } else {
          final ControlGroup group = constrainedWidget.getGroup();
          if(group != null) group.setType(ControlGroupType.ERROR);
          modal.addAlert(constrainedWidget.getPropertyLabel() + " " +
              validationMessageResolver.get(messageTemplate), AlertType.ERROR, new CloseHandler<AlertBase>() {
            @Override
            public void onClose(CloseEvent<AlertBase> event) {
              if(group != null) group.setType(ControlGroupType.NONE);
            }
          });
        }
      }
    }
  }

}
