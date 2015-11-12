#connection
require("RPostgreSQL")
pw <- {
  "Ucl2015-pu&"
}
drv <- dbDriver("PostgreSQL")
con <- dbConnect(drv, dbname = "londonair1",
                 host = "localhost", port = 5432,
                 user = "power_user", password = pw)
rm(pw)

#getting data with sql
df_avg_temp_2014 <- dbGetQuery(con, "select (extract (month from date)) as Month, AVG(value)
                               from pollution
                               where date >= '2014-01-01' 
                               and date < '2015-01-01'
                               and species_id = 'PM10'
                               group by Month
                               order by Month")
plot(df_avg_temp_2014, type="o", col="blue")
                
# close the connection
dbDisconnect(con)
dbUnloadDriver(drv)        