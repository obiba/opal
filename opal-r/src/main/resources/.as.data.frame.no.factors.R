.as.data.frame.no.factors <- function(x) {
  rval <- `x`
  if (!is.null(rval)) {
    rval <- as.data.frame(`x`, stringsAsFactors = FALSE)
    for (n in colnames(rval)) {
      if ("haven_labelled" %in% class(rval[[n]])) {
        rval[[n]] <- base::unclass(rval[[n]])
      }
    }
  }
  rval
}

