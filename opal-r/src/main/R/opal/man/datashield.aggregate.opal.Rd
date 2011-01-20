\name{datashield.aggregate.opal}
\alias{datashield.aggregate.opal}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{
%%  ~~function to do ... ~~
}
\description{
%%  ~~ A concise (1-5 lines) description of what the function does. ~~
}
\usage{
datashield.aggregate.opal(opal, aggregation, expr, resultName = "result")
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{opal}{
%%     ~~Describe \code{opal} here~~
}
  \item{aggregation}{
%%     ~~Describe \code{aggregation} here~~
}
  \item{expr}{
%%     ~~Describe \code{expr} here~~
}
  \item{resultName}{
%%     ~~Describe \code{resultName} here~~
}
}
\details{
%%  ~~ If necessary, more details than the description above ~~
}
\value{
%%  ~Describe the value returned
%%  If it is a LIST, use
%%  \item{comp1 }{Description of 'comp1'}
%%  \item{comp2 }{Description of 'comp2'}
%% ...
}
\references{
%% ~put references to the literature/web site here ~
}
\author{
%%  ~~who you are~~
}
\note{
%%  ~~further notes~~
}

%% ~Make other sections like Warning with \section{Warning }{....} ~

\seealso{
%% ~~objects to See Also as \code{\link{help}}, ~~~
}
\examples{
##---- Should be DIRECTLY executable !! ----
##-- ==>  Define data, use random,
##--	or do  help(data=index)  for the standard data sets.

## The function is currently defined as
function (opal, aggregation, expr, resultName = "result") 
{
    datashieldOpenSession(opal)
    url = paste(opal$url, "/jersey/datashield/R/", opal$ds_sid, 
        "/aggregate/", aggregation, sep = "")
    response = postForm(url, script = expr, aggregate = resultName, 
        curl = opal$h, style = "post")
    opal$h <- getCurlHandle()
    return(unserialize(response))
  }
}
% Add one or more standard keywords, see file 'KEYWORDS' in the
% R documentation directory.
\keyword{ ~kwd1 }
\keyword{ ~kwd2 }% __ONLY ONE__ keyword per line
