package org.obiba.opal.core.service.validation;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.magma.Value;
import org.obiba.opal.core.service.MagmaHelper;

/**
 *
 */
public class DataConstraintTest {

    @Test
    public void testMinValueSuccess() {
        DataConstraint dc = new MinValueConstraint(2);
        Value value = MagmaHelper.valueOf(2);
        Assert.assertTrue(dc.isValid(value));
    }

    @Test
    public void testMinValueFail() {
        DataConstraint dc = new MinValueConstraint(2.0);
        Value value = MagmaHelper.valueOf(1.9);
        Assert.assertFalse(dc.isValid(value));
    }

    @Test
    public void testMaxValueSuccess() {
        DataConstraint dc = new MaxValueConstraint(2.0);
        Value value = MagmaHelper.valueOf(2.0);
        Assert.assertTrue(dc.isValid(value));
    }

    @Test
    public void testMaxValueFail() {
        DataConstraint dc = new MaxValueConstraint(2);
        Value value = MagmaHelper.valueOf(3);
        Assert.assertFalse(dc.isValid(value));
    }

    @Test
    public void testPastDateSuccess() {
        DataConstraint dc = new PastDateConstraint();
        Value value = MagmaHelper.nowAsMagmaDate(-1000 * 60 * 60 * 24 * 2); //2 days ago
        Assert.assertTrue(dc.isValid(value));
    }

    @Test
    public void testPastDateFailure() {
        DataConstraint dc = new PastDateConstraint();
        Value value = MagmaHelper.nowAsDate(1000 * 60 * 60); //one hour in the future
        Assert.assertFalse(dc.isValid(value));
    }

}
