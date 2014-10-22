package org.obiba.opal.core.service.validation;

import org.obiba.magma.MagmaDate;
import org.obiba.magma.Value;

import java.util.Date;

/**
 *
 */
public class PastDateConstraint implements DataConstraint {

    @Override
    public String getType() {
        return "PastDate";
    }

    @Override
    public boolean isValid(Value value) {
        long now = System.currentTimeMillis();
        Long time = getTime(value);

        return time != null && (time.longValue() < now);
    }

    private Long getTime(Value value) {
        Object obj = value.getValue();
        if (obj instanceof MagmaDate) {
            MagmaDate dt = (MagmaDate)obj;
            return dt.asCalendar().getTimeInMillis();
        } else if (obj instanceof Date) {
            Date dt = (Date)obj;
            return dt.getTime();
        }

        return null; //null value or wrong type

    }

    @Override
    public String getMessage() {
        return String.format("Not past date");
    }

    @Override
    public String toString() {
        return "PastDate";
    }
}
