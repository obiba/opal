#
# Resource view utils
#

# get columns' name, class, type and attributes
.resource.get_columns <- function(x) {
  lapply(colnames(x), function(col) {
    attrs <- attributes(x[[col]])
    attrs$labels_names <- names(attrs$labels)
    klass <- x %>% select(col) %>% head(10) %>% pull() %>% class
    type <- x %>% select(col) %>% head(10) %>% pull() %>% tibble::type_sum()
    list(name=col, class=klass, type=type, attributes=attrs)
  })
}

# check the count of distinct ID values
.resource.is_multilines <- function(x, idColumn) {
  lgth <- length(x[[idColumn]])
  n <- dplyr::n_distinct(x[[idColumn]])
  lgth > n
}

# about dplyr programming
# https://cran.r-project.org/web/packages/dplyr/vignettes/programming.html

# get descriptive statistics
.resource.get_descriptive_stats <- function(.data, column) {
  .data %>%
    dplyr::filter(!is.na({{ column }})) %>%
    dplyr::select({{ column }}) %>%
    dplyr::summarise(
      mean = mean({{ column }}, na.rm = TRUE),
      n = n(),
      min = min({{ column }}, na.rm = TRUE),
      max = max({{ column }}, na.rm = TRUE),
      geomean = exp(mean(log({{ column }}), na.rm = TRUE)),
      stddev = sd({{ column }}),
      sum = sum({{ column }}, na.rm = TRUE),
      sumsq = sum(({{ column }})^2, na.rm = TRUE),
      variance = var({{ column }})
    ) %>%
    collect()
}

# get extended summary statistics
.resource.get_ext_descriptive_stats <- function(.data, column) {
  .data %>%
    dplyr::filter(!is.na({{ column }})) %>%
    dplyr::select({{ column }}) %>%
    dplyr::mutate(as.numeric({{ column }})) %>%
    dplyr::summarise(
      median = median({{ column }}, na.rm = TRUE),
      skewness = moments::skewness({{ column }}),
      kurtosis = moments::kurtosis({{ column }})
    ) %>%
    collect()
}

# get default frequencies: na or not
.resource.get_default_frequencies <- function(.data, column) {
  .data %>%
    dplyr::group_by(na = is.na({{ column }})) %>%
    dplyr::select({{ column }}) %>%
    dplyr::summarise(
      n = n(),
    ) %>%
    collect()
}

# get detailed frequencies: for each observed value
.resource.get_detailed_frequencies <- function(.data, column) {
  .data %>%
    dplyr::select({{ column }}) %>%
    dplyr::group_by({{ column }}) %>%
    dplyr::summarise(
      n = n(),
    ) %>%
    collect()
}

