package org.obiba.opal.web.gwt.app.client.support;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class PlaceRequestHelper {

  public static PlaceRequest.Builder createRequestBuilder(PlaceRequest request) {
    PlaceRequest.Builder builder = createRequestBuilderWithNameToken(request);

    for (String param : request.getParameterNames()) {
      builder.with(param, request.getParameter(param, ""));
    }

    return builder;
  }

  public static PlaceRequest.Builder createRequestBuilderWithParams(PlaceRequest request, List<String> included) {
    PlaceRequest.Builder builder = createRequestBuilderWithNameToken(request);

    for (String param : request.getParameterNames()) {
      if (included.indexOf(param) != -1) {
        builder.with(param, request.getParameter(param, ""));
      }
    }

    return builder;
  }

  public static PlaceRequest.Builder createRequestBuilderWithNameToken(PlaceRequest request) {
    return new PlaceRequest.Builder().nameToken(request.getNameToken());
  }
}
