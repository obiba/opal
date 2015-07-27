required.packages <- c("opal", "Rserve");
new.packages <- required.packages[
		!(required.packages %in% installed.packages()[,"Package"])];

if (length(new.packages))
    install.packages(new.packages, repos=c('http://cran.rstudio.com/', 'http://cran.obiba.org'), dependencies=TRUE);

