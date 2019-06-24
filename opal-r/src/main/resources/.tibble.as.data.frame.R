# transform a haven_labelled vector into a factor with appropritae levels (can be more than the observed values)
.tibble.as.data.frame <- function(x) {
    rval <- as.data.frame(`x`, stringsAsFactors=FALSE)
    for (n in colnames(rval)) {
        if ("haven_labelled" %in% class(rval[[n]])) {
            rval[[n]] <- factor(rval[[n]], levels = attributes(rval[[n]])$labels)
        }
    }
    rval
}