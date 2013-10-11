package org.obiba.opal.web.gwt.app.client.validation;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.validator.ValidationMessageResolver;
import org.obiba.opal.web.model.client.ws.ConstraintViolationErrorDto;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.github.gwtbootstrap.client.ui.event.ClosedEvent;
import com.github.gwtbootstrap.client.ui.event.ClosedHandler;
import com.google.common.collect.Maps;

public class ConstrainedModal {

//  private static final HibernateValidationMessageResolver hibernateValidationMessageResolver
//      = new HibernateValidationMessageResolver();

  private static final ValidationMessageResolver validationMessageResolver = new ValidationMessageResolver();

  private final Modal modal;

  private final Map<String, ConstrainedWidget> constrainedWidgets = Maps.newHashMap();

  public ConstrainedModal(@Nonnull Modal modal) {
    this.modal = modal;
    modal.addHandler(new ModalConstraintViolationErrorHandler(), ConstraintViolationErrorsEvent.getType());
  }

  public void registerWidget(@Nonnull String propertyPath, @Nonnull String propertyLabel) {
    registerWidget(propertyPath, propertyLabel, null);
  }

  public void registerWidget(@Nonnull String propertyPath, @Nonnull String propertyLabel,
      @Nullable ControlGroup group) {
    constrainedWidgets.put(propertyPath, new ConstrainedWidget(propertyPath, propertyLabel, group));
  }

  private static class ConstrainedWidget {

    @Nonnull
    private final String propertyPath;

    @Nonnull
    private final String propertyLabel;

    @Nullable
    private final ControlGroup group;

    private ConstrainedWidget(@Nonnull String propertyPath, @Nonnull String propertyLabel,
        @Nullable ControlGroup group) {
      this.propertyPath = propertyPath;
      this.propertyLabel = propertyLabel;
      this.group = group;
    }

    @Nullable
    private ControlGroup getGroup() {
      return group;
    }

    @Nonnull
    private String getPropertyPath() {
      return propertyPath;
    }

    @Nonnull
    private String getPropertyLabel() {
      return propertyLabel;
    }
  }

  private class ModalConstraintViolationErrorHandler
      implements ConstraintViolationErrorsEvent.ConstraintViolationErrorsHandler {

    @Override
    public void onConstraintViolationErrors(ConstraintViolationErrorsEvent event) {
      for(ConstraintViolationErrorDto violation : event.getViolations()) {
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
              validationMessageResolver.get(messageTemplate), AlertType.ERROR, new ClosedHandler() {
            @Override
            public void onClosed(ClosedEvent closedEvent) {
              if(group != null) group.setType(ControlGroupType.NONE);
            }
          });
        }
      }
    }
  }

}
