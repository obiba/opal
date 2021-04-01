# dump the output of the SQL query into a JSON object
.execute.SQL.JSON <- function(query, path = 'out.json') {
    toJSON <- function(df) {
        jsonlite::toJSON(list(columns = names(df), rows = df), dataframe = 'values', na = 'null')
    }
    conn <- file(path)
    writeLines(toJSON(sqldf::sqldf(query)), conn)
    close(conn)
}

# dump the output of the SQL query into a CSV file
.execute.SQL.CSV <- function(query, path = 'out.csv') {
    write.csv(sqldf::sqldf(query), file = path, row.names = FALSE, na = '')
}