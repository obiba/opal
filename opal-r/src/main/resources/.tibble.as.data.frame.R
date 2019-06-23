# transform a haven_labelled vector into a factor with appropritae levels (can be more than the observed values)
.tibble.as.data.frame <- function(x) {
    rval <- as.data.frame(`x`)
    for (n in colnames(rval)) {
        if ("haven_labelled" %in% class(rval[[n]])) {
            v <- as.factor(rval[[n]])
            levels(v) <- attributes(rval[[n]])$labels
            rval[[n]] <- v
        }
    }
    rval
}