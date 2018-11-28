package org.obiba.opal.spi.analysis;

import org.json.JSONObject;
import org.obiba.magma.Variable;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * An analysis represents the user data analysis request.
 */
public interface Analysis {

  /**
   * Unique analysis request identifier.
   *
   * @return
   */
  @NotNull
  String getId();

  /**
   * Analysis name.
   *
   * @return
   */
  @NotNull
  String getName();


  /**
   * The name of the plugin used to create this analysis.
   *
   * @return
   */
  String getPluginName();

  /**
   * Refers to one of the {@link AnalysisTemplate} made available by the analysis service.
   *
   * @return
   */
  @NotNull
  String getTemplateName();

  /**
   * The parameters to apply to the analysis instance.
   *
   * @return
   */
  JSONObject getParameters();

  /**
   * Get the variable names on which the analysis is to be applied. If empty, all variables of the table is potentially analysable.
   *
   * @return
   */
  List<String> getVariables();

}
