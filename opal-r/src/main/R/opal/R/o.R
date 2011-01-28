source("opal.R")
cag <- opal.login("http://localhost:8080", "administrator", "password")
ohs <- opal.login("http://localhost:8080", "administrator", "password")
opals<-list(CAG=cag, OHS=ohs)