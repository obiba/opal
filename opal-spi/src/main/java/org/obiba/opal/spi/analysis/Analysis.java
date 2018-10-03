package org.obiba.opal.spi.analysis;

import org.json.JSONObject;

import javax.validation.constraints.NotNull;

/**
 * An analysis represents the user data analysis request.
 */
public interface Analysis {

  /**
   * Analysis name.
   *
   * @return
   */
  String getName();

  /**
   * Refers to one of the {@link AnalysisTemplate} made available by the analysis service.
   *
   * @return
   */
  String getTemplateName();

  /**
   * The parameters to apply to the analysis instance.
   *
   * @return
   */
  @NotNull
  JSONObject getParameters();

}
