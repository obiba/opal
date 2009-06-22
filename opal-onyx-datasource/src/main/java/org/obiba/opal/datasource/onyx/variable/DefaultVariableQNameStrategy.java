package org.obiba.opal.datasource.onyx.variable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.obiba.onyx.engine.variable.IVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.Variable;

/**
 * A default implementation of {@link IVariableQNameStrategy}. This implementation removes the
 */
public class DefaultVariableQNameStrategy implements IVariableQNameStrategy {

  protected String baseUri;

  protected IVariablePathNamingStrategy variablePathNamingStrategy;

  protected boolean firstElementAsNamespace = true;

  public DefaultVariableQNameStrategy(String baseUri, IVariablePathNamingStrategy onyxNamingStrategy) {
    this.baseUri = baseUri;
    this.variablePathNamingStrategy = onyxNamingStrategy;
  }

  public void setBaseUri(String baseUri) {
    this.baseUri = baseUri;
  }

  public String getBaseUri() {
    return this.baseUri;
  }

  public void setVariablePathNamingStrategy(IVariablePathNamingStrategy variablePathNamingStrategy) {
    this.variablePathNamingStrategy = variablePathNamingStrategy;
  }

  public void setFirstElementAsNamespace(boolean firstElementAsNamespace) {
    this.firstElementAsNamespace = firstElementAsNamespace;
  }

  public QName getChildQName(String parentPath, String child) {
    String parentVariable = variablePathNamingStrategy.getVariablePath(parentPath);
    return getQName(parentVariable + variablePathNamingStrategy.getPathSeparator() + child);
  }

  public QName getParentQName(String child) {
    int index = child.lastIndexOf(variablePathNamingStrategy.getPathSeparator());
    if(index > 0) {
      return getQName(child.substring(0, index));
    }
    return null;
  }

  public QName getQName(String path) {
    List<String> parts = variablePathNamingStrategy.getNormalizedNames(path);

    // Remove the root (root is represented by baseUri)
    parts.remove(variablePathNamingStrategy.getRootName());

    if(parts.size() < 1) {
      throw new IllegalArgumentException("Cannot build QName from path, not enough parts: " + path);
    }

    StringBuilder namespace = new StringBuilder();
    namespace.append(baseUri);

    if(firstElementAsNamespace == true) {
      int length = namespace.length();
      if(namespace.charAt(length - 1) != '/') {
        namespace.append('/');
      }

      // First element is part of namespace
      String first = parts.remove(0);
      namespace.append(first);
      namespace.append('#');
      if(parts.size() == 0) {
        return new QName(namespace.toString(), first);
      }
    } else {
      namespace.append('#');
    }

    StringBuilder sb = new StringBuilder();
    for(String part : parts) {
      if(sb.length() > 0) {
        sb.append('.');
      }
      sb.append(part);
    }
    return new QName(namespace.toString(), sb.toString());
  }

  public QName getQName(Variable variable) {
    String path = variablePathNamingStrategy.getPath(variable);
    return getQName(path);
  }

  public QName getOccurenceVariable(String path) {
    Map<QName, String> occ = createOccurence(path);
    if(occ != null) {
      return occ.keySet().iterator().next();
    }
    return null;
  }

  public String getOccurenceIdentifier(String path) {
    Map<QName, String> occ = createOccurence(path);
    if(occ != null) {
      return occ.values().iterator().next();
    }
    return null;
  }

  private Map<QName, String> createOccurence(String path) {
    Map<String, String> parameters = variablePathNamingStrategy.getParameters(path);
    if(parameters != null && parameters.size() > 0) {
      String parentName = parameters.keySet().iterator().next();
      List<String> parts = variablePathNamingStrategy.getNormalizedNames(path);
      parts.remove(parts.size() - 1);
      Collections.reverse(parts);
      for(Iterator<String> iterator = parts.iterator(); iterator.hasNext();) {
        String string = iterator.next();
        if(string.equals(parentName) == false) {
          iterator.remove();
        } else {
          break;
        }
      }
      Collections.reverse(parts);

      StringBuilder parentPath = new StringBuilder();
      for(String part : parts) {
        if(parentPath.length() > 0) {
          parentPath.append(variablePathNamingStrategy.getPathSeparator());
        }
        parentPath.append(part);
      }

      String occurrenceId = parameters.values().iterator().next();
      Map<QName, String> occ = new HashMap<QName, String>();
      occ.put(getQName(parentPath.toString()), occurrenceId);
      return occ;
    }
    return null;
  }

}
