#install.packages("RPostgreSQL", dep=TRUE) #only need this the first time when not installed
require("RPostgreSQL")

# create a connection
# save the password that we can "hide" it as best as we can by collapsing it
pw <- {
  "Ucl2015-pu&"
}
dbDisconnect(con)
# loads the PostgreSQL driver
drv <- dbDriver("PostgreSQL")
# creates a connection to the postgres database
# note that "con" will be used later in each connection to the database
con <- dbConnect(drv, dbname = "londonair1",
                 host = "localhost", port = 5432,
                 user = "power_user", password = pw)
rm(pw) # removes the password
values <- dbGetQuery(con, "select pol.value, w.humidity, w.pressure, w.precip_mm, w.wind_speed_kmph, w.temperature_c from pollution pol left join weather w on pol.site_id = w.site_id and pol.date = w.date where pol.date >= '2013-04-01' and pol.date < '2015-04-03' and cast(w.date as time)>='08:00:00' and cast(w.date as time)<='11:00:00' and pol.species_id='PM25' and pol.site_id='BL0'" )

model = lm(value ~ humidity+pressure, data=values)

model1 = lm(value ~ humidity+pressure+precip_mm+wind_speed_kmph+temperature_c, data=values)

summary(model)
summary(model1)
anova(model, model1)
library(DAAG)

# check if table exists
dbExistsTable(con, "species")
# TRUE