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
valuesPM10 <- dbGetQuery(con, "select pol.value, w.temperature_c,w.precip_mm, w.cloud_cover, w.pressure, w.wind_direction_degree, w.wind_speed_kmph, w.humidity, w.wind_gust_kmph, w.feels_like_c, w.heat_index_c, w.visibility, w.wind_chill_c 
                  from pollution pol left join weather w on pol.site_id = w.site_id and pol.date = w.date where pol.date >= '2013-04-01' and pol.date < '2015-04-03' and pol.species_id='PM10' and pol.site_id='BL0'" )


step(lm(valuePM10 ~ temperature_c+precip_mm+cloud_cover+pressure+wind_direction_degree+wind_speed_kmph+humidity+wind_gust_kmph+feels_like_c+heat_index_c+visibility+wind_chill_c, data=values), direction = "backward")

valuesPM25 <- dbGetQuery(con, "select pol.value, w.temperature_c,w.precip_mm, w.cloud_cover, w.pressure, w.wind_direction_degree, w.wind_speed_kmph, w.humidity, w.wind_gust_kmph, w.feels_like_c, w.heat_index_c, w.visibility, w.wind_chill_c 
                  from pollution pol left join weather w on pol.site_id = w.site_id and pol.date = w.date where pol.date >= '2013-04-01' and pol.date < '2015-04-03' and pol.species_id='PM25' and pol.site_id='BL0'" )
step(lm(valuePM25 ~ temperature_c+precip_mm+cloud_cover+pressure+wind_direction_degree+wind_speed_kmph+humidity+wind_gust_kmph+feels_like_c+heat_index_c+visibility+wind_chill_c, data=values), direction = "backward")

library(DAAG)

modelPM25<-lm(formula = value ~ precip_mm + cloud_cover + pressure + wind_direction_degree + 
            humidity + wind_gust_kmph + feels_like_c + heat_index_c + 
            visibility + wind_chill_c, data = valuesPM25)
modelPM10<-lm(formula = value ~ precip_mm + cloud_cover + pressure + wind_direction_degree + wind_speed_kmph +
            humidity + wind_gust_kmph + feels_like_c + heat_index_c + 
            visibility + wind_chill_c, data = valuesPM10)
summary(modelPM25)
summary(modelPM10)
# check if table exists
dbExistsTable(con, "species")


# TRUE