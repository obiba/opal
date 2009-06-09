package org.obiba.opal.datasource.onyx.variable;

import javax.xml.namespace.QName;

import org.obiba.onyx.engine.variable.Variable;

/**
 * Strategy for building a {@link QName} (qualified name) from a
 * {@link Variable}
 */
public interface IVariableQNameStrategy {

  /**
   * Returns a {@code QName} for the specified {@code Variable}
   * 
   * @param variable
   *          the Onyx variable for which to obtain a {@code QName}
   * @return the {@code QName} for the variable.
   */
  public QName getQName(Variable variable);

  /**
   * Returns a {@code QName} for the specified {@code path}
   * 
   * @param path
   *          the variable path to convert to a {@code QName}
   * @return a {@code QName} for {@code path}
   */
  public QName getQName(String path);

  /**
   * Produce a QName for {@code child} starting from {@code parentPath}.
   * 
   * @param parentPath
   *          the parent path
   * @param child
   *          the child to add to the path
   * @return the {@code QName} for {@code child}
   */
  public QName getQName(String parentPath, String child);

  /**
   * Given a path with a reference to another variable, this method returns the
   * {@code QName} of the repeatable variable.
   * 
   * @param path
   *          the path to check for a reference
   * @return the {@code QName} of the occurrence, or null if path does not
   *         contain an occurrence reference.
   */
  public QName getOccurenceVariable(String path);

  /**
   * Given a path with a reference to another variable, this method returns the
   * id of the occurrence.
   * 
   * @param path
   *          the path to check for a reference
   * @return the unique id of the occurrence, or null if path does not contain
   *         an occurrence reference.
   */
  public String getOccurenceValue(String path);

}
