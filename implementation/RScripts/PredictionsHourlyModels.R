###general functions###


getWeatherData <- function(lat, lng) {
  
  require(jsonlite)
  baseURL <- "http://api.worldweatheronline.com/premium/v1/weather.ashx?"
  #apikey <- "key=1e27d9f1669f1cef172bb14e6e93e"
  apikey <- "key=2bcc1b65b4cbd22cae989bd27d8f7"
  coordinates <- paste("q=", lat, ",", lng, sep="")
  queries <- paste(apikey, "date=tomorrow", "num_of_days=2", "tp=1", "format=json", coordinates, sep="&")
  result <- fromJSON(paste(baseURL, queries, sep=""))
  return (result)
  
}

getSiteCoordinates <- function(site_id, df) {

#query <- paste("select lat, lng from site where site_id = '", site_id, "'", sep = "")
#return (query)

return (df[df$site_id == site_id, ])
}  

getSitesWithData <- function(species_id) {
  
  
  #select only sites that have data available
  sitesArray <- dbGetQuery(con, paste("select site_id,lat,lng from site where site_id IN
                                      (select distinct site_id from pollution
                                      where species_id = '", species_id,"' 
                                      order by site_id)", sep=""))
  
  return (sitesArray)
}

#### functions for NO2 ####
queryPollutionAndWeatherNO2 <- function(site_id, species_id, hour, date) {
  
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
                 " and pol.value >= 0",
                 daysSelectionQuery,
                 " and date_part('hour', pol.date) =",hour,
                 sep = "")
  #print(query)
  return (query)
}

linearRegressionModelSetupNO2 <- function(trainset, testset) {
  
    if(nrow(trainset) == 0) {
      return(-1)
    }
    if(species=="PM25"){
    model = lm(value ~ temperature_c+precip_mm+cloud_cover+pressure+wind_direction_degree+humidity+wind_gust_kmph+feels_like_c+heat_index_c+visibility+wind_chill_c,data=trainset)

	  
    }else{
 
    #model
    model = lm(value ~ temperature_c+precip_mm+cloud_cover+pressure+wind_direction_degree+wind_speed_kmph+humidity+wind_gust_kmph+feels_like_c+heat_index_c+visibility+wind_chill_c,data=trainset)
    } 
    #predict
    predicted <- predict(model, newdata = testset)
  return (predicted)
  
}

getTestsetForDateNO2 <- function(day = 1, df_weatherData, hour) {
  
  hour <- hour+1
  if(species=="PM25"){
   queryTestset_df <- data.frame(temperature_c = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["tempC"]][[hour]])),
	                             precip_mm = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["precipMM"]][[hour]])), 
	                             cloud_cover = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["cloudcover"]][[hour]])),
	                             pressure = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["pressure"]][[hour]])),
	                             wind_direction_degree = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["winddirDegree"]][[hour]])),
	                                humidity = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["humidity"]][[hour]])),
	                                wind_gust_kmph = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["WindGustKmph"]][[hour]])),
	                                feels_like_c = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["FeelsLikeC"]][[hour]])),
	                                heat_index_c = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["HeatIndexC"]][[hour]])),
	                                visibility = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["visibility"]][[hour]])),
	                                wind_chill_c = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["WindChillC"]][[hour]])))
  	
  }else{
 
  queryTestset_df <- data.frame(temperature_c = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["tempC"]][[hour]])),
                                precip_mm = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["precipMM"]][[hour]])), 
                                cloud_cover = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["cloudcover"]][[hour]])),
                                pressure = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["pressure"]][[hour]])),
                                wind_direction_degree = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["winddirDegree"]][[hour]])),
                                wind_speed_kmph = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["windspeedKmph"]][[hour]])),
                                humidity = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["humidity"]][[hour]])),
                                wind_gust_kmph = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["WindGustKmph"]][[hour]])),
                                feels_like_c = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["FeelsLikeC"]][[hour]])),
                                heat_index_c = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["HeatIndexC"]][[hour]])),
                                visibility = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["visibility"]][[hour]])),
                                wind_chill_c = c(as.numeric(df_weatherData[["data"]]$weather[["hourly"]][[day]][["WindChillC"]][[hour]])))
  }
  return (queryTestset_df)
  
}
species <- "NO2"
firstApplyofModel <- TRUE
dbAppend <- FALSE
dbOverwrite <- TRUE
setupModelforSiteAndSpeciesNO2ByHour <- function(hour, site_id, species_id, df_weatherData, date, day) {
  
  #days <- c(1, 2) #get all data for today and tomorrow
  
  #df_queryTestset <- lapply(day, getTestsetForDateNO2, df_weatherData = df_weatherData, hour = hour)
  df_queryTestset <- getTestsetForDateNO2(day, df_weatherData, hour)
  
  queryTrainset <- queryPollutionAndWeatherNO2(site_id, species_id, hour, date)
  
  df_queryTrainset <- dbGetQuery(con, queryTrainset)
  
  #call function
  
  predicted <- linearRegressionModelSetupNO2(df_queryTrainset, df_queryTestset)
  
  #if(predicted == -1) {
  #return (-1)
  #}
  #else 
  if(predicted < 0) {
    return (-1)
  }
  return (predicted)
}

setupModelforSiteAndSpeciesNO2 <- function(site_id, species_id, df_sites) {
  
  print(site_id)
  #working with weather data
  df_coordinates <- getSiteCoordinates(site_id, df_sites)
  df_weatherData <- getWeatherData(df_coordinates$lat, df_coordinates$lng)
  date_today <- df_weatherData[["data"]]$weather[["date"]][[1]]
  print(df_coordinates)
  date_tomorrow <- df_weatherData[["data"]]$weather[["date"]][[2]]
  
  hours <- c(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23)
  
  db_Update_today <- data.frame(site_id = c(site_id),
                                species_id = c(species_id),
                                date = c( paste(date_today, paste(sprintf("%02d", seq(from=00, to=23)), ":00:00", sep = ""))), ## generating the hours of the day
                                value = c(mapply(setupModelforSiteAndSpeciesNO2ByHour, hours, MoreArgs = list(species_id = species_id, site_id = site_id, df_weatherData = df_weatherData, date = date_today, day = 1))))
  
  db_Update_tomorow <- data.frame(site_id = c(site_id),
                                  species_id = c(species_id),
                                  date = c( paste(date_tomorrow, paste(sprintf("%02d", seq(from=00, to=23)), ":00:00", sep = ""))), ## generating the hours of the day
                                  value = c(mapply(setupModelforSiteAndSpeciesNO2ByHour, hours, MoreArgs = list(species_id = species_id, site_id = site_id, df_weatherData = df_weatherData, date = date_tomorrow, day = 2))))
  
  db_Update <- rbind(db_Update_today, db_Update_tomorow)
  #print(date)
  #print(queryTestset_df)
  
  
  print(dbWriteTable(con, "prediction", value = db_Update, append = dbAppend, overwrite = dbOverwrite, row.names = FALSE))
  
  if(firstApplyofModel == TRUE) {
    
    assign("dbAppend", TRUE, envir = .GlobalEnv)
    assign("dbOverwrite", FALSE, envir = .GlobalEnv)
    assign("firstApplyofModel", FALSE, envir = .GlobalEnv)
    
  }
  
  
  # close the connection
  #on.exit(dbDisconnect(con))
  
}

setupAllModels <- function() {
  
  #NO2
  sitesArray <- getSitesWithData("NO2")
  #print(is.data.frame(sitesArray))
  #print(sitesArray[, 1])
  mapply(setupModelforSiteAndSpeciesNO2, sitesArray[,1], MoreArgs = list(species_id = "NO2", df_sites = sitesArray))
  
  #PM10
  assign("species", "PM10", envir = .GlobalEnv)
  sitesArray <- getSitesWithData("PM10")
  #print(sitesArray[, 1])
  mapply(setupModelforSiteAndSpeciesNO2, sitesArray[,1], MoreArgs = list(species_id = "PM10", df_sites = sitesArray))
  
  
  #PM2.5
  assign("species", "PM25", envir = .GlobalEnv)
  sitesArray <- getSitesWithData("PM25")
  #print(sitesArray[, 1])
  mapply(setupModelforSiteAndSpeciesNO2, sitesArray[,1], MoreArgs = list(species_id = "PM25", df_sites = sitesArray))
  dbDisconnect(con)
  
}

####START-OF-SCRIPT####

#connection
require("RPostgreSQL")
drv <- dbDriver("PostgreSQL")

pw <- {
  "Ucl2015-pu&"
}
con <- dbConnect(drv, dbname = "londonair1",
                 host = "localhost", port = 5432,
                 user = "power_user", password = pw)
rm(pw)
print(system.time(setupAllModels(),TRUE))

#to close all connections

mapply(dbDisconnect, dbListConnections(dbDriver("PostgreSQL")))
dbUnloadDriver(drv)
