.execute.SQL <- function(query) {
    sqlTableToJSON <- function(df) {
        jsonlite::toJSON(list(columns = names(df), rows = df), dataframe = 'values', na = 'null')
    }
    tryCatch(sqlTableToJSON(sqldf::sqldf(query)), error = function(e) paste0('{ "error": ', jsonlite::toJSON(e$message, auto_unbox = TRUE), '}'))
}