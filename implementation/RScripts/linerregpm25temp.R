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
df_pm25_temp_042014 <- dbGetQuery(con, "select pol.value, w.temperature_c 
                                    from pollution pol
                                    left join weather w 
                                    on pol.site_id = w.site_id 
                                    and pol.date = w.date
                                    where pol.site_id = 'BL0'
                                    and pol.species_id = 'PM25' 
                                    and pol.date >= '2014-04-01' 
                                    and pol.date < '2014-05-01'")


df_pm25_temp_week1052014 <- dbGetQuery(con, "select pol.value, w.temperature_c 
                                    from pollution pol
                                       left join weather w 
                                       on pol.site_id = w.site_id 
                                       and pol.date = w.date
                                       where pol.site_id = 'BL0'
                                       and pol.species_id = 'PM25' 
                                       and pol.date >= '2014-05-01' 
                                       and pol.date < '2014-05-08'")


# close the connection
dbDisconnect(con)
dbUnloadDriver(drv) 


model = lm(value ~ temperature_c, data=df_pm25_temp_042014)
summary(model)

#plot
plot(df_pm25_temp_042014)
abline(model)


#predict
df_predicted_week1062015 <- predict(model, newdata = df_pm25_temp_week1052014)
cbind(df_pm25_temp_week1052014 ,df_predicted_week1062015)