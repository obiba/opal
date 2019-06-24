# transform a haven_labelled vector into a factor with appropritae levels (can be more than the observed values)
.tibble.as.data.frame <- function(x) {
    rval <- as.data.frame(`x`, stringsAsFactors=FALSE)
    for (n in colnames(rval)) {
        if ("haven_labelled" %in% class(rval[[n]]) && !is.null(attributes(rval[[n]])$labels)) {
            v <- factor(rval[[n]], levels = attributes(rval[[n]])$labels)
            # restore some attributes
            attributes(v)$labels <- attributes(rval[[n]])$labels
            if (!is.null(attributes(rval[[n]])$label)) {
                attributes(v)$label <- attributes(rval[[n]])$label
            }
            if (!is.null(attributes(rval[[n]])$na_values)) {
                attributes(v)$na_values <- attributes(rval[[n]])$na_values
            }
            if (!is.null(attributes(rval[[n]])$opal.value_type)) {
                attributes(v)$opal.value_type <- attributes(rval[[n]])$opal.value_type
            }
            rval[[n]] <- v
        }
    }
    rval
}