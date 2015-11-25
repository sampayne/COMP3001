getWeatherData <- function(lat,lng) {
  
  require(jsonlite)
  baseURL <- "http://api.worldweatheronline.com/premium/v1/weather.ashx?"
  apikey <- "key=1e27d9f1669f1cef172bb14e6e93e"
  coordinates <- paste("q=", lat, ",", lng, sep="")
  queries <- paste(apikey, "date=tomorrow", "num_of_days=2", "tp=1", "format=json", coordinates, sep="&")
  result <- fromJSON(paste(baseURL, queries, sep=""))
  return (result)
  
}

getSiteCoordinates <- function(site_id) {
  
  query <- paste("select lat, lng from site where site_id = '", site_id, "'", sep = "")
  return (query)
  
}  

queryPollutionAndWeather <- function(site_id, species_id, startH, endH) {
  
  print(site_id)
  
  if(format(as.Date(date),"%w") != 0 && format(as.Date(date),"%w") != 6) {
    
    daysSelectionQuery <- " and extract(dow from pol.date) != 0
    and extract(dow from pol.date) !=6"
    
  }
  else {
    
    daysSelectionQuery <- " and extract(dow from pol.date) = 0
    and extract(dow from pol.date) =6"
    
  }
  
  query <- paste("select pol.value, w.temperature_c,w.precip_mm, w.cloud_cover, w.pressure, w.wind_direction_degree, w.wind_speed_kmph, w.humidity, w.wind_gust_kmph, w.feels_like_c, w.heat_index_c, w.visibility, w.wind_chill_c 
                 from pollution pol
                 left join weather w 
                 on pol.site_id = w.site_id
                 and pol.date = w.date
                 where pol.site_id = '",
                 site_id,
                 "' and pol.species_id = '",
                 species_id,
                 "'",daysSelectionQuery,
                 " and date_part('hour', pol.date) >=",startH," and date_part('hour', pol.date) <=", endH,
                 sep = "")
  #print(query)
  return (query)
}


linearRegressionModelSetup <- function(trainset, testset) {
  
  #model
  model = lm(value ~ temperature_c+precip_mm+cloud_cover+pressure+wind_direction_degree+wind_speed_kmph+humidity+wind_gust_kmph+feels_like_c+heat_index_c+visibility+wind_chill_c,data=trainset)
  print(summary(model))
  
  #predict
  predicted <- predict(model, newdata = testset)
  #print(cbind(testset, predicted))
  #print(predicted)
  #plot true vs. prediction
  #plot(testset, predicted)
  return (predicted)
  
}


firstApplyofModel <- TRUE
dbAppend <- TRUE
dbOverwrite <- FALSE
setupModelforSiteAndSpecies <- function(site_id, species_id, startH, endH) {
  
  #connection
  pw <- {
    "Ucl2015-pu&"
  }
  con <- dbConnect(drv, dbname = "londonair1",
                   host = "localhost", port = 5432,
                   user = "power_user", password = pw)
  rm(pw)
  
  queryTrainset <- queryPollutionAndWeather(site_id, species_id, "'2014-04-01'",
                                            "'2014-05-01'")
  
  df_queryTrainset <- dbGetQuery(con, queryTrainset)
  
  #working with weather data
  df_coordinates <- dbGetQuery(con, getSiteCoordinates(site_id))
  df_weatherData <- getWeatherData(df_coordinates$lat, df_coordinates$lng)
  
  queryTestset_df <- data.frame(temperature_c = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[1]][["tempC"]])),
                                precip_mm = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[1]][["precipMM"]])), 
                                cloud_cover = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[1]][["cloudcover"]])),
                                pressure = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[1]][["pressure"]])),
                                wind_direction_degree = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[1]][["winddirDegree"]])),
                                wind_speed_kmph = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[1]][["windspeedKmph"]])),
                                humidity = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[1]][["humidity"]])),
                                wind_gust_kmph = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[1]][["WindGustKmph"]])),
                                feels_like_c = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[1]][["FeelsLikeC"]])),
                                heat_index_c = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[1]][["HeatIndexC"]])),
                                visibility = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[1]][["visibility"]])),
                                wind_chill_c = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[1]][["WindChillC"]])))
  
  date <- df_weatherData[["data"]]$weather[["date"]][[1]]
  
  
  
  #call function
  predicted <- linearRegressionModelSetup(df_queryTrainset,queryTestset_df)
  
  db_Update <- data.frame(site_id = c(site_id),
                          species_id = c(species_id),
                          date = c(paste(date, paste(seq(from=0, to=23), ":00:00", sep = ""))), ## generating the hours of the day
                          value = c(predicted))
  print(date)
  print(queryTestset_df)
  print(db_Update)
  
  
  print(dbWriteTable(con, "prediction", value = db_Update, append = dbAppend, overwrite = dbOverwrite, row.names = FALSE))
  
  if(firstApplyofModel == TRUE) {
    
    dbAppend <- TRUE
    dbOverwrite <- FALSE
    firstApplyofModel <- FALSE
  }
  
  
  # close the connection
  on.exit(dbDisconnect(con))
  
}

####START-OF-SCRIPT####

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

#select only sites that have data available
sitesArray <- dbGetQuery(con, "select distinct site_id from pollution
                         where species_id = 'PM25'
                         order by site_id")

# close the connection
dbDisconnect(con)



#getting data with sql

site_id <- "BL0"
species_id <- "PM25"


#setupModelforSiteAndSpecies(site_id,species_id)
#weatherData <- getWeatherData()

mapply(setupModelforSiteAndSpecies, sitesArray[3:5, ], MoreArgs = list(species_id = species_id))



#to close all connections
mapply(dbDisconnect, dbListConnections(dbDriver("PostgreSQL")))
dbUnloadDriver(drv)
