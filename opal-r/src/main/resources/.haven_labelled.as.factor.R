# transform a haven_labelled vector into a factor with appropritae levels (can be more than the observed values)
.haven_labelled.as.factor <- function(x) {
    rval <- `x`
    for (n in colnames(rval)) {
        if ("haven_labelled" %in% class(rval[[n]])) {
            v <- as.factor(rval[[n]])
            levels(v) <- attributes(rval[[n]])$labels
            rval[[n]] <- v
        }
    }
    rval
}