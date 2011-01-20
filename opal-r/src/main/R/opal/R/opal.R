opal.login <- function(url,username,password) {
  require(RCurl)
  require(rjson)
  opal <- new.env(parent=globalenv())
  opal$url <- url

  # cookielist="" activates the cookie engine
  opal$curl <- curlSetOpt(verbose=TRUE, header=TRUE, httpheader=c(Accept="application/octet-stream, application/json", Authorization=.authToken(username, password)), cookielist="")
  opal$reader <- dynCurlReader(curl=opal$curl)
  class(opal) <- "opal"

  return(opal)
}

# Utility method to build urls. Concatenates all arguments and adds a '/' separator between each element
.url <- function(opal, ..., query=list()) {
	.tmp <- paste(opal$url, "ws", paste(sapply(c(...), curlEscape), collapse="/"), sep="/")
	if(length(query)) {
		.params <- paste(sapply(names(query), function(id) paste(id, curlEscape(query[[id]]), sep = "=")), collapse = "&")
		.tmp <- paste(.tmp, .params, sep="?")
	}
	.tmp
}

# Constructs the value for the Authorization header
.authToken <- function(username, password) {
	paste("X-Opal-Auth", base64(paste(username, password, sep=":")))
}

# Issues a request to opal for the specified resource
.get <- function(opal, ..., query=list()) {
	curlSetOpt(httpget=TRUE, customrequest=NULL, curl=opal$curl)
	.perform(opal, .url(opal, ..., query=query))
}

.post <- function(opal, ..., params=c()) {
	.nobody <- missing(params) || length(params) == 0
	if(.nobody) {
		# Act like a GET, but send a POST. This is required when posting without any body 
		curlSetOpt(httpget=TRUE, customrequest="POST", curl=opal$curl)
	} else {
		curlSetOpt(post=TRUE, header=TRUE, postfields=params, curl=opal$curl)
	}
	.perform(opal, .url(opal, ...))
}

.put <- function(opal, ..., body='') {
	.nobody <- missing(params) || length(params) == 0
	if(.nobody) {
		# Act like a GET, but send a PUT. This is required when posting without any body 
		curlSetOpt(httpget=TRUE, customrequest="PUT", curl=opal$curl)
	} else {
		curlSetOpt(post=TRUE, header=TRUE, postfields=body, curl=opal$curl)
	}
	.perform(opal, .url(opal, ...))
}

.delete <- function(opal, ...) {
	# Act like a GET, but send a DELETE.
	curlSetOpt(httpget=TRUE, customrequest="DELETE", curl=opal$curl)
	.perform(opal, .url(opal, ...))
}

.perform <- function(opal, url) {
	opal$reader <- dynCurlReader(opal$curl)

	handle <- opal$curl
	curlPerform(url=url, writefunction=opal$reader$update,  curl=handle)
	content <- opal$reader$value()
	header <- parseHTTPHeader(opal$reader$header())
	info <- getCurlInfo(handle)
	.handleResponse(list(code=info$response.code, content.type=info$content.type, cookielist=info$cookielist, content=content, headers=header))
}

.handleResponse <- function(response) {
	if(response$code >= 400 && response$code < 500) {
		print(paste("Invalid request: ", response$code))
		NULL
	}	else if(response$code >= 500) {
		print(paste("Server error: ", response$code))
		NULL
	} else {
		if(length(grep("octet-stream", response$content.type))) {
			unserialize(response$content)
		} else if(length(grep("json", response$content.type))) {
			fromJSON(readChar(response$content, length(response$content)))
		}
	}
}

.extractJsonField <- function(json, fields) {
	if(is.null(fields)) {
	  json 
	} else {
	  lapply(json, function(obj) {obj[fields]})
	}
}

opal.datasources=function(opal, fields=NULL) {
	.extractJsonField(.get(opal, "datasources"), fields)
}

opal.tables <- function(opal, datasource, fields=NULL) {
	.extractJsonField(.get(opal, "datasource", datasource, "tables"), fields);
}

opal.variables <- function(opal, datasource, table, fields=NULL) {
	.extractJsonField(.get(opal, "datasource", datasource, "table", table, "variables"), fields)
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