# transform a haven_labelled vector into a factor with appropritae levels (can be more than the observed values)
.with.factors <- function(x, as.data.frame = TRUE) {
    rval <- `x`
    if (as.data.frame) {
        rval <- as.data.frame(`x`, stringsAsFactors=FALSE)
    }
    for (n in colnames(rval)) {
        if ("haven_labelled" %in% class(rval[[n]]) && !is.null(attributes(rval[[n]])$labels)) {
            v <- factor(rval[[n]], levels = attributes(rval[[n]])$labels)
            # restore attributes (without conflicting with factor's ones)
            for (attr in names(attributes(rval[[n]]))) {
                if (!(attr %in% c("levels", "class"))) {
                    attributes(v)[[attr]] <- attributes(rval[[n]])[[attr]]
                }
            }
            rval[[n]] <- v
        }
    }
    rval
}