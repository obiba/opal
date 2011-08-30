#-------------------------------------------------------------------------------
# Copyright (c) 2011 OBiBa. All rights reserved.
#  
# This program and the accompanying materials
# are made available under the terms of the GNU Public License v3.0.
#  
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#-------------------------------------------------------------------------------
.onLoad <- function(libname, pkgname) {
  require(RCurl)
  require(rjson)
}

# Utility method to build urls. Concatenates all arguments and adds a '/' separator between each element
.url <- function(opal, ..., query=list()) {
	.tmp <- paste(opal$url, "ws", paste(sapply(c(...), curlEscape), collapse="/"), sep="/")
	if(length(query)) {
		.params <- paste(sapply(names(query), function(id) paste(id, curlEscape(query[[id]]), sep = "="), simplify=FALSE), collapse = "&")
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

.post <- function(opal, ..., query=list(), body='') {
	.nobody <- missing(body) || length(body) == 0
	if(.nobody) {
		# Act like a GET, but send a POST. This is required when posting without any body 
		opts = curlOptions(httpget=TRUE, customrequest="POST", .opts=opal$opts)
	} else {
		opts = curlOptions(post=TRUE, customrequest=NULL, postfields=body, .opts=opal$opts)
	}
	.perform(opal, .url(opal, ..., query=query), opts)
}

.put <- function(opal, ..., query=list(), body='', contentType='application/x-rscript') {
	.nobody <- missing(body) || length(body) == 0
	if(.nobody) {
		# Act like a GET, but send a PUT. This is required when posting without any body 
		opts = curlOptions(httpget=TRUE, customrequest="PUT", .opts=opal$opts)
	} else {
		opts = curlOptions(post=TRUE, httpheader=c(opal$opts$httpheader, 'Content-Type'=contentType), postfields=body, customrequest="PUT", .opts=opal$opts)
	}
	.perform(opal, .url(opal, ..., query=query), opts)
}

.delete <- function(opal, ..., query=list()) {
	# Act like a GET, but send a DELETE.
	opts = curlOptions(httpget=TRUE, customrequest="DELETE", .opts=opal$opts)
	.perform(opal, .url(opal, ..., query=query), opts)
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
          if(is.raw(response$content)) {
            fromJSON(readChar(response$content, length(response$content)));
          } else {
            fromJSON(response$content);
          }
		}
	}
}

.extractJsonField <- function(json, fields, isArray=TRUE) {
	if(is.null(fields)) {
	  json 
	} else {
		if(isArray) {
          lapply(l, function(obj) {obj[fields]})
		} else {
			json[fields]
  		}
	}
}

# returns a list r such that r[[i]] == l[[i]][field] for all i:length(l)
.select <- function(l, field) {
  lapply(l, function(obj) {obj[[field]]})
}

.opal.login <- function(username,password,url,opts=list()) {
  opal <- new.env(parent=globalenv())
  
  # Strip trailing slash
  opal$url <- sub("/$", "", url)
  
  # cookielist="" activates the cookie engine
  headers <- c(Accept="application/octet-stream, application/json");
  if(is.null(username) == FALSE) {
    headers <- c(headers, Authorization=.authToken(username, password));
  }
  opal$opts <- curlOptions(header=TRUE, httpheader=headers, cookielist="", .opts=opts)
  opal$curl <- curlSetOpt(.opts=opal$opts)
  opal$reader <- dynCurlReader(curl=opal$curl)
  class(opal) <- "opal"

  opal
}

opal.login <- function(username = NULL,password = NULL,url,opts=list()) {
  if(is.list(url)){
    lapply(url, function(u){opal.login(username, password, u, opts=opts)})
  } else {
    .opal.login(username, password, url, opts)
  }
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
