/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.rserve;

import com.google.common.collect.Lists;
import org.json.JSONObject;
import org.obiba.opal.spi.r.RMatrix;
import org.obiba.opal.spi.r.RNamedList;
import org.obiba.opal.spi.r.RRuntimeException;
import org.obiba.opal.spi.r.RServerResult;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPLogical;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Direct connection with Rserve, through its java API.
 */
class RserveResult implements RServerResult {

  private static final Logger log = LoggerFactory.getLogger(RserveResult.class);

  private final REXP result;

  public RserveResult(REXP result) {
    this.result = result;
  }

  @Override
  public int length() {
    try {
      return result.length();
    } catch (REXPMismatchException e) {
      return -1;
    }
  }

  @Override
  public boolean isRaw() {
    return result.isRaw();
  }

  @Override
  public byte[] asBytes() {
    if (result.isRaw()) {
      try {
        return result.asBytes();
      } catch (REXPMismatchException e) {
        throw new RRuntimeException(e);
      }
    }
    return new byte[0];
  }

  @Override
  public boolean isNumeric() {
    return result.isNumeric();
  }

  @Override
  public double[] asDoubles() {
    if (isNumeric()) {
      try {
        return result.asDoubles();
      } catch (REXPMismatchException e) {
        throw new RRuntimeException(e);
      }
    }
    return new double[0];
  }

  @Override
  public boolean isInteger() {
    return result.isInteger();
  }

  @Override
  public int[] asIntegers() {
    if (isInteger()) {
      try {
        return result.asIntegers();
      } catch (REXPMismatchException e) {
        throw new RRuntimeException(e);
      }
    }
    return new int[0];
  }

  @Override
  public boolean asLogical() {
    if (result.isLogical()) {
      REXPLogical logical = (REXPLogical) result;
      return logical.length() > 0 && logical.isTRUE()[0];
    }
    return false;
  }

  @Override
  public String asJSON() {
    return asStrings()[0];
  }

  @Override
  public boolean isNull() {
    return result == null || result.isNull();
  }

  @Override
  public boolean isString() {
    return result.isString();
  }

  @Override
  public String[] asStrings() {
    try {
      return result.asStrings();
    } catch (REXPMismatchException e) {
      throw new RRuntimeException(e);
    }
  }

  @Override
  public RMatrix<String> asStringMatrix() {
    return new RserveStringMatrix(result);
  }

  @Override
  public boolean isList() {
    return result.isList();
  }

  @Override
  public List<RServerResult> asList() {
    List<RServerResult> rval = Lists.newArrayList();
    if (result.isList()) {
      try {
        for (Object obj : result.asList()) {
          rval.add(new RserveResult((REXP) obj));
        }
      } catch (REXPMismatchException e) {
        throw new RRuntimeException(e);
      }
    }
    return rval;
  }

  @Override
  public boolean isNamedList() {
    try {
      return result.isList() && result.asList().isNamed();
    } catch (REXPMismatchException e) {
      return false;
    }
  }

  @Override
  public RNamedList<RServerResult> asNamedList() {
    try {
      return isNamedList() ? new RserveNamedList(result) : new RserveNamedList(null);
    } catch (REXPMismatchException e) {
      throw new RRuntimeException(e);
    }
  }

  @Override
  public boolean[] isNA() {
    try {
      return result.isNA();
    } catch (REXPMismatchException e) {
      throw new RRuntimeException(e);
    }
  }

  @Override
  public Object asNativeJavaObject() {
    try {
      return result.asNativeJavaObject();
    } catch (REXPMismatchException e) {
      throw new RRuntimeException(e);
    }
  }

  @Override
  public boolean hasNames() {
    return isNamedList() || result.hasAttribute("names");
  }

  @Override
  public String[] getNames() {
    try {
      if (isNamedList()) {
        String[] namesStr = new String[result.asList().names.size()];
        int i = 0;
        for (Object name : result.asList().names) {
          namesStr[i++] = name == null ? null : name.toString();
        }
        return namesStr;
      } else {
        return result.getAttribute("names").asStrings();
      }
    } catch (REXPMismatchException e) {
      throw new RRuntimeException(e);
    }
  }
}
