#-------------------------------------------------------------------------------
# Copyright (c) 2011 OBiBa. All rights reserved.
#  
# This program and the accompanying materials
# are made available under the terms of the GNU Public License v3.0.
#  
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#-------------------------------------------------------------------------------
datashield.newSession <- function(opal) {
  UseMethod('datashield.newSession');
}

datashield.newSession.opal <- function(opal) {
  .extractJsonField(.post(opal, "datashield", "sessions"), c("id"), isArray=FALSE)
}

datashield.newSession.list <- function(opals) {
  lapply(opals, FUN=datashield.newSession.opal)
}

datashield.setSession <- function(opal, ...) {
  UseMethod('datashield.setSession');
}

datashield.setSession.opal <- function(opal, sessionId) {
  .put(opal, "datashield", "session", sessionId, "current");
}

datashield.setSession.list <- function(opals, sessionId) {
  lapply(opals, FUN=datashield.setSession.opal, sessionId)
}

# Sends a script, and calls "summary" on the result.
datashield.summary=function(object, ...) {
  UseMethod('datashield.summary');
}

datashield.summary.opal=function(opal, expr) {
  return(datashield.aggregate.opal(opal, as.call(c(quote(summary), expr))))
}

datashield.summary.list=function(opals, expr) {
  lapply(opals, FUN=datashield.summary.opal, expr)
}

# Sends a script, and calls "length" on the result.
datashield.length=function(object, ...) {
  UseMethod('datashield.length');
}

datashield.length.opal=function(opal, expr) {
  return(datashield.aggregate.opal(opal, as.call(c(quote(length), expr))))
}

datashield.length.list=function(opals, expr) {
  lapply(opals, FUN=datashield.length.opal, expr)
}

datashield.aggregate=function(object, ...) {
  UseMethod('datashield.aggregate');
}

# Inner methods that sends a script, and aggregates the result using the specified aggregation method
datashield.aggregate.opal=function(opal, expr) {
  expression = expr
  # convert a call to a string
  if(is.language(expr)) {
    expression <- .deparse(expr)
  } else if(! is.character(expr) ) {
    return(print(paste("Invalid expression type: '", class(value), "'. Expected a call or character vector.", sep="")))
  }

  .post(opal, "datashield", "session", "current", "aggregate", body=expression, contentType="application/x-rscript")
}

datashield.aggregate.list=function(opals, expr) {
  lapply(opals, FUN=datashield.aggregate.opal, expr)
}

datashield.assign=function(object, ...) {
  UseMethod('datashield.assign');
}

datashield.assign.opal=function(opal, symbol, value) {
  if(is.language(value) || is.function(value)) {
    contentType <- "application/x-rscript"
    body <- .deparse(value)
  } else if(is.character(value)) {
    contentType <- "application/x-opal"
    body <- value
  } else {
    return(print(paste("Invalid value type: '", class(value), "'. Use quote() to protect from early evaluation.", sep="")))
  }

  .put(opal, "datashield", "session", "current", "symbol", symbol, body=body, contentType=contentType)
  # Return the new symbols length
  datashield.length(opal, as.symbol(symbol))
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

datashield.rm=function(object, ...) {
  UseMethod('datashield.rm');
}

datashield.rm.list=function(opals, ...) {
  lapply(opals, FUN=datashield.rm.opal, ...)
}

datashield.rm.opal=function(opal, symbol) {
  .delete(opal, "datashield", "session", "current", "symbol", symbol)
}

datashield.lm=function(object, ...) {
  UseMethod('datashield.lm');
}

datashield.lm.list=function(opals, formula, lmparams=list()) {

  numstudies<-length(opals)

  call<-as.call(c(quote(lm), formula, lmparams))

  studies.summary<-datashield.aggregate(opals, 'lm.ds', call)
  cat("\nSUMMARY OF MODEL STATE FOR EACH STUDY")
  print(studies.summary)

  beta.s<-as.matrix(as.data.frame(lapply(studies.summary, function(i) {i$coefficients[,1]})))
  se.s<-as.matrix(as.data.frame(lapply(studies.summary, function(i) {i$coefficients[,2]})))

  # Fetch the number of participants per study
  numsubs.study<-unlist(.select(studies.summary, 'numsubs'))
  analysis.wt<-numsubs.study/sum(numsubs.study)

  #set up a vector of 1s to use in summing precisions
  simple.sum<-rep(1,numstudies)

  #calculate mean of regression coefficients weighted for study sample sizes
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
  colnames(meta.analysis.results)<-c("Estimate","Std. Error")

  meta.analysis.results
}

datashield.glm=function(object, ...) {
  UseMethod('datashield.glm');
}

datashield.glm.list=function(opals, formula, family, maxit=10) {

  numstudies<-length(opals)

  beta.vect.next<-NULL

  #Iterations need to be counted. Start off with the count at 0
  #and increment by 1 at each new iteration
  iteration.count<-0

  #Provide arbitrary starting value for deviance to enable subsequent calculation of the
  #change in deviance between iterations
  dev.old<-9.99e+99

  #Convergence state needs to be monitored.
  converge.state<-FALSE

  #Define a convergence criterion. This value of epsilon corresponds to that used
  #by default for GLMs in R (see section S3 for details)
  epsilon<-1.0e-08
  
  f<-NULL

  while(!converge.state && iteration.count < maxit) {

    iteration.count<-iteration.count+1

    cat("--------------------------------------------\n")
    cat("Iteration", iteration.count, "\n")

    call<-as.call(list(quote(glm.ds), formula, family, as.vector(beta.vect.next)));

    study.summary<-datashield.aggregate(opals, call);

    info.matrix.total<-Reduce(f="+", .select(study.summary, 'info.matrix'))
    score.vect.total<-Reduce(f="+", .select(study.summary, 'score.vect'))
    dev.total<-Reduce(f="+", .select(study.summary, 'dev'))
    
    if(iteration.count==1) {
      # Sum participants only during first iteration.
      nsubs.total<-Reduce(f="+", .select(study.summary, 'numsubs'))
      # Save family
      f <- study.summary[[1]]$family
    }

    #Create variance covariance matrix as inverse of information matrix
    variance.covariance.matrix.total<-solve(info.matrix.total)

    #Create beta vector update terms
    beta.update.vect<-variance.covariance.matrix.total %*% score.vect.total

    #Add update terms to current beta vector to obtain new beta vector for next iteration
    if(is.null(beta.vect.next)) {
      beta.vect.next<-beta.update.vect
    } else {
      beta.vect.next<-beta.vect.next+beta.update.vect
    }

    #Calculate value of convergence statistic and test whether meets convergence criterion
    converge.value<-abs(dev.total-dev.old)/(abs(dev.total)+0.1)
    if(converge.value<=epsilon)converge.state<-TRUE
    if(converge.value>epsilon)dev.old<-dev.total
    
    #For ALL iterations summarise model state after current iteration
    cat("\nSUMMARY OF MODEL STATE after iteration No",iteration.count,
        "\n\nCurrent deviance",dev.total,"on",
        (nsubs.total-length(beta.vect.next)), "degrees of freedom",
        "\nConvergence criterion    ",converge.state," (", converge.value,")\n\n")
    
    cat("beta\n")
    print(as.vector(beta.vect.next))
    
    cat("Information matrix overall\n")
    print(info.matrix.total)
    
    cat("Score vector overall\n")
    print(score.vect.total)
    
    cat("Current Deviance\n")
    print(dev.total)
    cat("--------------------------------------------\n")    
  }

  #If convergence has been obtained, declare final (maximum likelihood) beta vector,
  #and calculate the corresponding standard errors, z scores and p values
  #(the latter two to be consistent with the output of a standard GLM analysis)
  #Then print out final model summary
  if(converge.state)
  {
    beta.vect.final<-beta.vect.next

    scale.par <- 1
    if(f$family== 'gaussian') {
      scale.par <- dev.total / (nsubs.total-length(beta.vect.next))
    }

    se.vect.final <- sqrt(diag(variance.covariance.matrix.total)) * sqrt(scale.par)

    z.vect.final<-beta.vect.final/se.vect.final
    pval.vect.final<-2*pnorm(-abs(z.vect.final))
    parameter.names<-names(score.vect.total[,1])
    model.parameters<-cbind(beta.vect.final,se.vect.final,z.vect.final,pval.vect.final)
    dimnames(model.parameters)<-list(parameter.names,c("Estimate","Std. Error","z-value","p-value"))

    glmds <- list(
        formula=formula,
        coefficients=model.parameters,
        dev=dev.total,
        nsubs=nsubs.total,
        df=(nsubs.total-length(beta.vect.next)),
        iter=iteration.count
    )

    class(glmds) <- 'glmds'

    glmds   
  } else {
    warning(paste("Did not converge after", maxit, "iterations. Increase maxit parameter as necessary."))
    NULL
  }
}

# Override print for the result of datashield.glm
print.glmds <-function (x, digits = max(3, getOption("digits") - 3), ...) {
  #If converged print out final model summary
  cat('Formula:\n')
  print(x$formula)
  cat("\nCoefficients:\n")
  print.default(format(x$coefficients, digits = digits), print.gap = 2, quote = FALSE)
  cat("\nDeviance:",x$dev)
  cat("\nDegrees of Freedom:", x$df)
  cat("\nIterations:", x$iter, "\n")
}

.deparse <- function(expr) {
  expression <- deparse(expr)
  if(length(expression) > 1) {
    expression = paste(expression, collapse='\n')
  }
  expression
}