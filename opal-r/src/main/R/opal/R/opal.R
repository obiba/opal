.onLoad <- function(libname, pkgname) {
  require(RCurl)
  require(rjson)
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

opal.login <- function(url,username,password,opts=list()) {
	opal <- new.env(parent=globalenv())

	# Strip trailing slash
	opal$url <- sub("/$", "", url)

	# cookielist="" activates the cookie engine
	opal$opts <- curlOptions(header=TRUE, httpheader=c(Accept="application/octet-stream, application/json", Authorization=.authToken(username, password)), cookielist="", .opts=opts)
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