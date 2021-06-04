.datashield.find <- function() {
  dsFields <- c('AggregateMethods', 'AssignMethods', 'Options')

  # extract DS fields from DESCRIPTION files
  pkgs <- Map(function(p) {
    x <- as.list(p)
    x[names(x) %in% dsFields]
  }, Filter(function(p) any(names(p) %in% dsFields),
            lapply(installed.packages()[, 1],
                   function(p) as.data.frame(read.dcf(system.file('DESCRIPTION', package = p)), stringsAsFactors = FALSE))))

  # extract DS fields from DATASHIELD files
  x <- lapply(installed.packages()[, 1], function(p) system.file('DATASHIELD', package = p))
  y <- lapply(x[lapply(x, nchar) > 0], function(f) as.list(as.data.frame(read.dcf(f), stringsAsFactors = FALSE)))

  # merge and prepare DS field values as arrays of strings
  pkgs <- lapply(append(pkgs, y), function(p)
    lapply(p, function(pp) gsub('^\\s+|\\s+$', '', gsub('\n', '', unlist(strsplit(pp, ','))))))
  pkgs
}