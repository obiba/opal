opal.login=function(url,username,password) {
  require(RCurl);
  loginUrl <- paste(url, "/login", sep="");
  response <- getURL(loginUrl, userpwd=paste(username,":",password), verbose=TRUE);
  opal <- new.env(parent=globalenv());
  opal$url <- url;
  opal$h <- getCurlHandle();
  class(opal) <- "opal";
  return(opal)
}

readTable=function(objbect, ...) {
  UseMethod('readTable');
}

readTable.opal=function(opal, ds, table, vars=NULL) {
  require(rjson)
  require(utils)
  url <- paste(opal$url,"/jersey/datasource/",ds,"/",table,"/values.json",sep="")
  if(is.null(vars) == FALSE) {
    url <- paste(url, "?v=", paste(unlist(vars), collapse="&v="), sep="");
  }
  response <- getURL(url, curl=opal$h);
  opal$h <- getCurlHandle();
  return(fromJSON(response))
}

show=function(objbect, ...) {
UseMethod('show');
}

show.opal=function(opal) {
  url <- paste(opal$url,"/jersey/datasources",sep="")
  response <- getURL(url, curl=opal$h)
  opal$h <- getCurlHandle();
  return(response)
}

# Sends a script, and calls "summary" on the result.
datashield.summary=function(object, ...) {
  UseMethod('datashield.summary');
}

datashield.summary.opal=function(opal, expr, resultName="result") {
  return(datashield.aggregate.opal(opal, "summary", expr, resultName))
}

datashield.summary.list=function(opals, expr, resultName="result") {
  return(datashield.aggregate.list(opals, "summary", expr, resultName))
}

# Sends a script, and calls "length" on the result.
datashield.length=function(object, ...) {
  UseMethod('datashield.length');
}

datashield.length.opal=function(opal, expr, resultName="result") {
  return(datashield.aggregate.opal(opal, "length", expr, resultName))
}

datashield.length.list=function(opals, expr, resultName="result") {
  r=datashield.aggregate.list(opals, "length", expr, resultName);
  # Transform the list into a vector of ints
  r=unlist(r);
  # Assign names to the vector
  names(r)=names(opals);
  return(r);
}

# Inner methods that sends a script, and aggregates the "result" symbol using the aggregation method
datashield.aggregate.opal=function(opal, aggregation, expr, resultName="result") {
  datashieldOpenSession(opal);

  url=paste(opal$url,"/jersey/datashield/R/", opal$ds_sid, "/aggregate/", aggregation, sep="")
  response=postForm(url, script=expr, aggregate=resultName, curl=opal$h, style="post");
  opal$h <- getCurlHandle();
  return(unserialize(response));
}

datashield.aggregate.list=function(opals, aggregation, expr, resultName="result") {
  o=list();
  for(opal in names(opals)) {
    print(paste("Evaluating for", opal));
    o[[opal]]=datashield.aggregate.opal(opals[[opal]], aggregation, expr, resultName);
  }
  return(o);
}

# Tells Datashield to assign Opal Variables to R symbols
datashield.assign=function(object, ...) {
  UseMethod('datashield.assign');
}

datashield.assign.opal=function(opal, ...) {
  datashieldOpenSession(opal);
  
  url=paste(opal$url,"/jersey/datashield/R/", opal$ds_sid, "/assign", sep="")
  response=postForm(url, curl=opal$h, .params=list(...), style="post");
  opal$h <- getCurlHandle();
  print(response);
}

# Opens a Datashield session if it doesn't already exists
datashieldOpenSession=function(object, ...) {
  UseMethod('datashieldOpenSession');
}

datashieldOpenSession.opal=function(opal, ...) {
 if(is.null(opal$ds_sid)) {
    response <- postForm(paste(opal$url, "/jersey/datashield/R", sep=""), bogus="value", curl=opal$h);
    opal$ds_sid<-response[1];
  }
}