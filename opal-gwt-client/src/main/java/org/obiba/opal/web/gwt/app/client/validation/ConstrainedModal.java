package org.obiba.opal.web.gwt.app.client.validation;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.hibernate.validator.HibernateValidationMessageResolver;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.model.client.ws.ConstraintViolationErrorDto;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.github.gwtbootstrap.client.ui.event.ClosedEvent;
import com.github.gwtbootstrap.client.ui.event.ClosedHandler;
import com.google.common.collect.Maps;
import com.google.gwt.user.client.ui.Widget;

public class ConstrainedModal {

  private static final HibernateValidationMessageResolver validationMessageResolver
      = new HibernateValidationMessageResolver();

  private final Modal modal;

  private final Map<String, ConstrainedWidget> constrainedWidgets = Maps.newHashMap();

  public ConstrainedModal(@Nonnull Modal modal) {
    this.modal = modal;
    modal.addHandler(new ModalConstraintViolationErrorHandler(), ConstraintViolationErrorsEvent.getType());
  }

  public void registerWidget(@Nonnull String propertyPath, @Nonnull String propertyLabel, @Nonnull Widget widget) {
    registerWidget(propertyPath, propertyLabel, widget, null);
  }

  public void registerWidget(@Nonnull String propertyPath, @Nonnull String propertyLabel, @Nonnull Widget widget,
      @Nullable ControlGroup group) {
    constrainedWidgets.put(propertyPath, new ConstrainedWidget(propertyPath, propertyLabel, widget, group));
  }

  private static class ConstrainedWidget {

    @Nonnull
    private final String propertyPath;

    @Nonnull
    private final String propertyLabel;

    @Nonnull
    private final Widget widget;

    @Nullable
    private final ControlGroup group;

    private ConstrainedWidget(@Nonnull String propertyPath, @Nonnull String propertyLabel, @Nonnull Widget widget,
        @Nullable ControlGroup group) {
      this.propertyPath = propertyPath;
      this.propertyLabel = propertyLabel;
      this.widget = widget;
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
    private Widget getWidget() {
      return widget;
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
        ConstrainedWidget constrainedWidget = constrainedWidgets.get(violation.getPropertyPath());
        if(constrainedWidget == null) {
          // this error is not attached to a widget, default
          modal.addAlert(validationMessageResolver.get(violation.getMessageTemplate()), AlertType.ERROR);
        } else {
          final ControlGroup group = constrainedWidget.getGroup();
          if(group != null) group.setType(ControlGroupType.ERROR);
          modal.addAlert(constrainedWidget.getPropertyLabel() + " " +
              validationMessageResolver.get(violation.getMessageTemplate()), AlertType.ERROR, new ClosedHandler() {
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
