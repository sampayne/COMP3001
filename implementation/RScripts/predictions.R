###general functions###


getWeatherData <- function(lat, lng) {
  
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

getSitesWithData <- function(species_id) {
  pw <- {
    "Ucl2015-pu&"
  }
  con <- dbConnect(drv, dbname = "londonair1",
                   host = "localhost", port = 5432,
                   user = "power_user", password = pw)
  rm(pw)
  
  #select only sites that have data available
  sitesArray <- dbGetQuery(con, paste("select distinct site_id from pollution
                                      where species_id = '", species_id,"' 
                                      order by site_id", sep=""))
  
  on.exit(dbDisconnect(con))
  
  return (sitesArray)
  
  
}


#### functions for NO2 ####
queryPollutionAndWeatherNO2 <- function(site_id, species_id, startH, endH, date) {
  
  #print(site_id)
  
  if(format(as.Date(date),"%w") != 0 && format(as.Date(date),"%w") != 6) {
    
    daysSelectionQuery <- " and extract(dow from pol.date) != 0
    and extract(dow from pol.date) !=6"
    
  }
  else {
    
    daysSelectionQuery <- " and (extract(dow from pol.date) = 0
    or extract(dow from pol.date) =6)"
    
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
                 "'",
#                  daysSelectionQuery,
#                  " and date_part('hour', pol.date) >=",startH," and date_part('hour', pol.date) <=", endH,
                 sep = "")
  #print(query)
  return (query)
}


linearRegressionModelSetupNO2 <- function(trainset, testset) {
  
  #model
  model = lm(value ~ temperature_c+precip_mm+cloud_cover+pressure+wind_direction_degree+wind_speed_kmph+humidity+wind_gust_kmph+feels_like_c+heat_index_c+visibility+wind_chill_c,data=trainset)
  #print(summary(model))
  
  #predict
  predicted <- predict(model, newdata = testset)
  return (predicted)
  
}


getTestsetForDateNO2 <- function(day = 1, df_weatherData) {
  
  queryTestset_df <- data.frame(temperature_c = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["tempC"]])),
                                precip_mm = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["precipMM"]])), 
                                cloud_cover = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["cloudcover"]])),
                                pressure = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["pressure"]])),
                                wind_direction_degree = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["winddirDegree"]])),
                                wind_speed_kmph = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["windspeedKmph"]])),
                                humidity = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["humidity"]])),
                                wind_gust_kmph = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["WindGustKmph"]])),
                                feels_like_c = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["FeelsLikeC"]])),
                                heat_index_c = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["HeatIndexC"]])),
                                visibility = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["visibility"]])),
                                wind_chill_c = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["WindChillC"]])))
  
  return (queryTestset_df)
  
}

firstApplyofModel <- TRUE
dbAppend <- FALSE
dbOverwrite <- TRUE
setupModelforSiteAndSpeciesNO2 <- function(site_id, species_id,  startH = 0, endH = 24) {
  
  #connection
  pw <- {
    "Ucl2015-pu&"
  }
  con <- dbConnect(drv, dbname = "londonair1",
                   host = "localhost", port = 5432,
                   user = "power_user", password = pw)
  rm(pw)
  
  
  
  #working with weather data
  df_coordinates <- dbGetQuery(con, getSiteCoordinates(site_id))
  df_weatherData <- getWeatherData(df_coordinates$lat, df_coordinates$lng)
  days <- c(1, 2) #get all data for today and tomorrow
  
  df_queryTestset <- lapply(days, getTestsetForDateNO2, df_weatherData = df_weatherData)
  #print(df_result[[2]])
  
  date_today <- df_weatherData[["data"]]$weather[["date"]][[1]]
  date_tomorrow <- df_weatherData[["data"]]$weather[["date"]][[2]]
  queryTrainset_today <- queryPollutionAndWeatherNO2(site_id, species_id, startH, endH, date_today)
  queryTrainset_tomorrow <- queryPollutionAndWeatherNO2(site_id, species_id, startH, endH, date_tomorrow)
  #print(queryTrainset_today)
  df_queryTrainset_today <- dbGetQuery(con, queryTrainset_today)
  df_queryTrainset_tomorrow <- dbGetQuery(con, queryTrainset_tomorrow)
  
  #call function
  predicted_today <- linearRegressionModelSetupNO2(df_queryTrainset_today, df_queryTestset[[1]])
  predicted_tomorrow <- linearRegressionModelSetupNO2(df_queryTrainset_tomorrow, df_queryTestset[[2]])
  
  db_Update_today <- data.frame(site_id = c(site_id),
                          species_id = c(species_id),
                          date = c( paste(date_today, paste(seq(from=0, to=23), ":00:00", sep = ""))), ## generating the hours of the day
                          value = c(predicted_today))
  db_Update_tomorow <- data.frame(site_id = c(site_id),
                                species_id = c(species_id),
                                date = c( paste(date_tomorrow, paste(seq(from=0, to=23), ":00:00", sep = ""))), ## generating the hours of the day
                                value = c(predicted_tomorrow))
  
  db_Update <- rbind(db_Update_today, db_Update_tomorow)
  #print(date)
  #print(queryTestset_df)
  #print(db_Update)
  
  
  print(dbWriteTable(con, "prediction", value = db_Update, append = dbAppend, overwrite = dbOverwrite, row.names = FALSE))
  
  if(firstApplyofModel == TRUE) {
    
    assign("dbAppend", TRUE, envir = .GlobalEnv)
    assign("dbOverwrite", FALSE, envir = .GlobalEnv)
    assign("firstApplyofModel", FALSE, envir = .GlobalEnv)

  }
  
  
  # close the connection
  on.exit(dbDisconnect(con))
  
}


setupAllModels <- function() {
  
  #NO2
  sitesArray <- getSitesWithData("NO2")
  #print(sitesArray[, 1])
  mapply(setupModelforSiteAndSpeciesNO2, sitesArray[, 1], MoreArgs = list(species_id = "NO2"))
  
  #PM10
  
  
  #PM2.5
  
  
}

####START-OF-SCRIPT####

#connection
require("RPostgreSQL")
drv <- dbDriver("PostgreSQL")
setupAllModels()

#to close all connections
#mapply(dbDisconnect, dbListConnections(dbDriver("PostgreSQL")))
dbUnloadDriver(drv)
