.with.factors <- function(x, as.data.frame = TRUE) {
    rval <- `x`
    if (!is.null(rval)) {
        if (as.data.frame) {
            rval <- as.data.frame(`x`, stringsAsFactors=FALSE)
        }
        for (n in colnames(rval)) {
            attrs <- attributes(rval[[n]])
            if ("haven_labelled" %in% class(rval[[n]]) && !is.null(attrs$labels)) {
                if (attrs$opal.nature == 'CATEGORICAL') {
                    v <- labelled::to_factor(rval[[n]], levels = "values")
                    # restore attributes (without conflicting with factor's ones)
                    for (attr in names(attrs)) {
                        if (!(attr %in% c("levels", "class"))) {
                            attributes(v)[[attr]] <- attrs[[attr]]
                        }
                    }
                    rval[[n]] <- v
                }
                else if (attrs$opal.nature == 'CONTINUOUS') {
                    rval[[n]] <- base::unclass(rval[[n]])
                }
            }
            # do not want haven things in data frame (plus datashield supports only a single class)
            else if (as.data.frame) {
                attributes(rval[[n]])[["class"]] <- attrs[["class"]][1]
            }
        }
    }
    rval
}