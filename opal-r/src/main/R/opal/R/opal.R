# Utility method to build urls. Concatenates all arguments and adds a '/' separator between each element
.url <- function(opal, ..., query=list()) {
#	.tmp <- paste(opal$url, "ws", paste(sapply(c(...), curlEscape), collapse="/"), sep="/")
	.tmp <- paste(opal$url, "ws", ..., sep="/")
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
	opts = curlOptions(httpget=TRUE, customrequest=NULL, .opts=opal$opts)
	.perform(opal, .url(opal, ..., query=query), opts)
}

.post <- function(opal, ..., params=c()) {
	.nobody <- missing(params) || length(params) == 0
	if(.nobody) {
		# Act like a GET, but send a POST. This is required when posting without any body 
		opts = curlOptions(httpget=TRUE, customrequest="POST", .opts=opal$opts)
	} else {
		opts = curlOptions(post=TRUE, customrequest=NULL, postfields=params, .opts=opal$opts)
	}
	.perform(opal, .url(opal, ...), opts)
}

.put <- function(opal, ..., body='', contentType='application/x-rscript') {
	.nobody <- missing(body) || length(body) == 0
	if(.nobody) {
		# Act like a GET, but send a PUT. This is required when posting without any body 
		opts = curlOptions(httpget=TRUE, customrequest="PUT", .opts=opal$opts)
	} else {
		opts = curlOptions(post=TRUE, httpheader=c(opal$opts$httpheader, 'Content-Type'=contentType), postfields=body, customrequest="PUT", .opts=opal$opts)
	}
	.perform(opal, .url(opal, ...), opts)
}

.delete <- function(opal, ...) {
	# Act like a GET, but send a DELETE.
	opts = curlOptions(httpget=TRUE, customrequest="DELETE", .opts=opal$opts)
	.perform(opal, .url(opal, ...), opts)
}

.perform <- function(opal, url, opts) {
	opal$reader <- dynCurlReader(opal$curl)

	handle <- opal$curl
	curlPerform(url=url, .opts=opts, writefunction=opal$reader$update,  curl=handle)
	content <- opal$reader$value()
	header <- parseHTTPHeader(opal$reader$header())
	info <- getCurlInfo(handle)
	.handleResponse(list(code=info$response.code, content.type=info$content.type, cookielist=info$cookielist, content=content, headers=header))
}

.handleResponse <- function(response) {
	if(response$code >= 400 && response$code < 500) {
		print(paste("Invalid request(", response$code, "):", response$content))
		NULL
	}	else if(response$code >= 500) {
		print(paste("Server error: ", response$code, " ", response$content))
		NULL
	} else {
		if(length(grep("octet-stream", response$content.type))) {
			unserialize(response$content)
		} else if(length(grep("json", response$content.type))) {
			fromJSON(readChar(response$content, length(response$content)))
		}
	}
}

.extractJsonField <- function(json, fields, isArray=TRUE) {
	if(is.null(fields)) {
	  json 
	} else {
		if(isArray) {
			lapply(json, function(obj) {obj[fields]})
		} else {
			json[fields]
  		}
	}
}

opal.login <- function(url,username,password) {
	require(RCurl)
	require(rjson)
	opal <- new.env(parent=globalenv())
	# TODO: strip trailing / if any
	opal$url <- url
	
	# cookielist="" activates the cookie engine
	opal$opts <- curlOptions(verbose=TRUE, header=TRUE, httpheader=c(Accept="application/octet-stream, application/json", Authorization=.authToken(username, password)), cookielist="")
	opal$curl <- curlSetOpt(.opts=opal$opts)
	opal$reader <- dynCurlReader(curl=opal$curl)
	class(opal) <- "opal"
	
	return(opal)
}

opal.newSession <- function(opal) {
	.extractJsonField(.post(opal, "r", "sessions"), c("id"), isArray=FALSE)
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

datashield.newSession <- function(opal) {
	UseMethod('datashield.newSession');
}

datashield.newSession.opal <- function(opal) {
	.extractJsonField(.post(opal, "datashield", "sessions"), c("id"), isArray=FALSE)
}

datashield.newSession.list <- function(opals) {
	lapply(opals, FUN=datashield.newSession.opal)
}

# Sends a script, and calls "summary" on the result.
datashield.summary=function(object, ...) {
  UseMethod('datashield.summary');
}

datashield.summary.opal=function(opal, expr) {
  return(datashield.aggregate.opal(opal, "summary", expr))
}

datashield.summary.list=function(opals, expr) {
	lapply(opals, FUN=datashield.summary.opal, expr)
}

# Sends a script, and calls "length" on the result.
datashield.length=function(object, ...) {
  UseMethod('datashield.length');
}

datashield.length.opal=function(opal, expr) {
  return(datashield.aggregate.opal(opal, "length", expr))
}

datashield.length.list=function(opals, expr) {
	lapply(opals, FUN=datashield.length.opal, expr)
}

datashield.aggregate=function(object, ...) {
	UseMethod('datashield.aggregate');
}

# Inner methods that sends a script, and aggregates the result using the specified aggregation method
datashield.aggregate.opal=function(opal, aggregation, expr) {
	expression  = expr
	if(is.language(expr)) expression = deparse(expr)
	
	.post(opal, "datashield", "session", "current", "aggregate", aggregation, params=expression)
}

datashield.aggregate.list=function(opals, aggregation, expr) {
	lapply(opals, FUN=datashield.aggregate.opal, aggregation, expr)
}

datashield.assign=function(object, ...) {
  UseMethod('datashield.assign');
}

datashield.assign.opal=function(opal, symbol, value) {
	if(is.language(value)) {
		contentType <- "application/x-rscript"
		body <- deparse(value)
	} else {
		contentType <- "application/x-opal"
		body <- value
	}
	.put(opal, "datashield", "session", "current", "symbol", symbol, body=body, contentType=contentType)
}

datashield.assign.list=function(opals, ...) {
	lapply(opals, FUN=datashield.assign.opal, ...)
}

datashield.symbols=function(object, ...) {
	UseMethod('datashield.symbols');
}

datashield.symbols.opal=function(opal) {
	.get(opal, "datashield", "session", "current", "symbols")
}

datashield.symbols.list=function(opals) {
	lapply(opals, FUN=datashield.symbols.opal)
}
