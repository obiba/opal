# dump the output of the SQL query into a JSON object
.execute.SQL.JSON <- function(query, path = 'out.json') {
    if (!require('sqldf')) { install.packages('sqldf', repos = "https://cloud.r-project.org") }
    toJSON <- function(df) {
        jsonlite::toJSON(list(columns = names(df), rows = df), dataframe = 'values', na = 'null', digits = NA)
    }
    conn <- file(path)
    writeLines(toJSON(sqldf::sqldf(query)), conn)
    close(conn)
}

# dump the output of the SQL query into a CSV file
.execute.SQL.CSV <- function(query, path = 'out.csv') {
    if (!require('sqldf')) { install.packages('sqldf', repos = "https://cloud.r-project.org") }
    write.csv(sqldf::sqldf(query), file = path, row.names = FALSE, na = '')
}

# dump the output of the SQL query into a RDS file
.execute.SQL.RDS <- function(query, path = 'out.rds') {
    if (!require('sqldf')) { install.packages('sqldf', repos = "https://cloud.r-project.org") }
    saveRDS(sqldf::sqldf(query), file = path)
}