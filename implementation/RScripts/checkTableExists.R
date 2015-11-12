#install.packages("RPostgreSQL", dep=TRUE) #only need this the first time when not installed
require("RPostgreSQL")

# create a connection
# save the password that we can "hide" it as best as we can by collapsing it
pw <- {
  "Ucl2015-pu&"
}

# loads the PostgreSQL driver
drv <- dbDriver("PostgreSQL")
# creates a connection to the postgres database
# note that "con" will be used later in each connection to the database
con <- dbConnect(drv, dbname = "londonair1",
                 host = "localhost", port = 5432,
                 user = "power_user", password = pw)
rm(pw) # removes the password

# check if table exists
dbExistsTable(con, "species")
# TRUE

# close the connection
dbDisconnect(con)
dbUnloadDriver(drv)  