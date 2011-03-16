datashield.newSession <- function(opal) {
  UseMethod('datashield.newSession');
}

datashield.newSession.opal <- function(opal) {
  .extractJsonField(.post(opal, "datashield", "sessions"), c("id"), isArray=FALSE)
}

datashield.newSession.list <- function(opals) {
  lapply(opals, FUN=datashield.newSession.opal)
}

# Sends a script, and calls "coefficients" on the result.
datashield.coefficients=function(object, ...) {
  UseMethod('datashield.coefficients');
}

datashield.coefficients.opal=function(opal, expr) {
  return(datashield.aggregate.opal(opal, "coefficients", expr))
}

datashield.coefficients.list=function(opals, expr) {
  lapply(opals, FUN=datashield.coefficients.opal, expr)
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
  } else if(is.character(value)) {
    contentType <- "application/x-opal"
    body <- value
  } else {
    return(print(paste("Invalid value type: '", class(value), "'. Use quote() to protect from early evaluation.", sep="")))
  }
  .put(opal, "datashield", "session", "current", "symbol", symbol, body=body, contentType=contentType)
  # Return the new symbols length
  datashield.length(opal, symbol)
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

datashield.lm=function(object, ...) {
  UseMethod('datashield.lm');
}

datashield.lm.list=function(opals, formula, ...) {

  numstudies<-length(opals)

  # for Y ~ X + Z, terms will be c("X", "Z")
  # for Y ~ X * Z, terms will be c("X", "Z", "X:Z")
  terms<-unlist(dimnames(attr(terms(formula), "factors"))[2])

  # numpara includes Intercept
  numpara<-length(terms)+1

  beta.s<-matrix(NA,nrow=numpara,ncol=numstudies)
  se.s<-matrix(NA,nrow=numpara,ncol=numstudies)

  model<-substitute(lm(f), list(f=formula))

  for(k in 1:numstudies) {
    study.summary<-datashield.aggregate(opals[[k]], "summary", model)
    beta.s[,k]<-study.summary$coefficients[,1]
    se.s[,k]<-study.summary$coefficients[,2]
  }

  # Fetch the length for each study 
  numsubs.study<-unlist(datashield.length(opals, terms[1]))
  numsubs<-sum(numsubs.study)
  analysis.wt<-numsubs.study/numsubs
  
  #set up a vector of 1s to use in summing precisions
  simple.sum<-rep(1,numstudies)
  
  #calculate mean of regression coefficients weighted for study sample sizes
  # “%*%” denotes vector multiplication 
  beta.overall<-beta.s%*%analysis.wt
  
  #convert standard errors into precisions
  precision.s<-1/(se.s)^2
  
  #sum precisions across studies
  precision.overall<-precision.s%*%simple.sum
  
  #convert precisions back to standard errors
  se.overall<-1/(precision.overall)^0.5 

  #create output results matrix
  meta.analysis.results<-cbind(beta.overall,se.overall)

  # Set dimension names
  dimnames(meta.analysis.results)<-list(c("(Intercept)", terms),c("Coefficients","Std. Error"))

  meta.analysis.results
}


datashield.glm.list=function(opals, formula, ...) {
  
}