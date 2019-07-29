# transform a haven_labelled vector into a factor with appropritae levels (can be more than the observed values)
.with.factors <- function(x, as.data.frame = TRUE) {
    rval <- `x`
    if (as.data.frame) {
        rval <- as.data.frame(`x`, stringsAsFactors=FALSE)
    }
    for (n in colnames(rval)) {
        attrs <- attributes(rval[[n]])
        if (attrs$opal.nature == 'CATEGORICAL' && "haven_labelled" %in% class(rval[[n]]) && !is.null(attrs$labels)) {
            v <- factor(rval[[n]], levels = attrs$labels)
            # restore attributes (without conflicting with factor's ones)
            for (attr in names(attrs)) {
                if (!(attr %in% c("levels", "class"))) {
                    attributes(v)[[attr]] <- attrs[[attr]]
                }
            }
            rval[[n]] <- v
        }
        # do not want haven things in data frame (plus datashield supports only a single class)
        if (as.data.frame) {
            attributes(rval[[n]])[["class"]] <- attrs[["class"]][1]
        }
    }
    rval
}