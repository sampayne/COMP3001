var moment = require('moment');
var request = require('request');
var async = require('async');
module.exports = function(Routes) {

	Routes.predictIndexGivenRouteID = function(routeID, numOfMinsStayTimeAtWork, startDateTimeForPrediction, cb){
		Routes.predictIndexGivenRouteIDStayingAtHome(routeID, numOfMinsStayTimeAtWork, startDateTimeForPrediction, function(err, indexHomeResult){
			Routes.predictIndexGivenRouteIDGoingToWork(routeID, numOfMinsStayTimeAtWork, startDateTimeForPrediction, function(err, indexWorkResult){
				var result = {
					'stayingAtHome' : indexHomeResult,
					'goingToWork' : indexWorkResult
				}
				cb(null, result);
			})
		})
	}
	Routes.predictIndexGivenRouteIDStayingAtHome = function(routeID, numOfMinsStayTimeAtWork, startDateTimeForPrediction, cb){
		Routes.findOne({where: {id: routeID}}, function(err, routeObj) { 
			//change date times to start with prediction start time
			var initialDateTime =  new Date(routeObj.route[0].time).getTime();
			var startDateTimeForPredictionObj = new Date(startDateTimeForPrediction).getTime();
			for(coord in routeObj.route){
				var currentDateTime = new Date(routeObj.route[coord].time).getTime();
				console.log(currentDateTime);
				console.log(initialDateTime);
				console.log(startDateTimeForPredictionObj);
				var diff = new Date((currentDateTime - initialDateTime) + startDateTimeForPredictionObj);
				console.log(diff);
				routeObj.route[coord].time = moment(diff).format("YYYY-MM-DD HH:mm:ss");
			}
			console.log(routeObj);

			Routes.calcindexforstayingathome(routeObj.route, numOfMinsStayTimeAtWork, function(err, indexResult){
				cb(null,indexResult);
			})
		});
	}
	Routes.predictIndexGivenRouteIDGoingToWork = function(routeID, numOfMinsStayTimeAtWork, startDateTimeForPrediction, cb){
		Routes.findOne({where: {id: routeID}}, function(err, routeObj) { 
			//change date times to start with prediction start time
			var initialDateTime =  new Date(routeObj.route[0].time).getTime();
			var startDateTimeForPredictionObj = new Date(startDateTimeForPrediction).getTime();
			for(coord in routeObj.route){
				var currentDateTime = new Date(routeObj.route[coord].time).getTime();
				console.log(currentDateTime);
				console.log(initialDateTime);
				console.log(startDateTimeForPredictionObj);
				var diff = new Date((currentDateTime - initialDateTime) + startDateTimeForPredictionObj);
				console.log(diff);
				routeObj.route[coord].time = moment(diff).format("YYYY-MM-DD HH:mm:ss");
			}
			console.log(routeObj);

			Routes.calcindexforgoingtowork(routeObj.route, numOfMinsStayTimeAtWork, function(err, indexResult){
				cb(null,indexResult);
			})
		});
	}
	Routes.calcIndexGivenStayTime = function(routeObj, numOfMinsStayTimeAtWork, cb){
		Routes.calcindexforstayingathome(routeObj, numOfMinsStayTimeAtWork, function(err, indexHomeResult){
			Routes.calcindexforgoingtowork(routeObj, numOfMinsStayTimeAtWork, function(err, indexWorkResult){
				var result = {
					'stayingAtHome' : indexHomeResult,
					'goingToWork' : indexWorkResult
				}
				cb(null, result);
			})
		})
	}

	Routes.calcindexforstayingathome = function(routeObj,numOfMinsAtWork, cb){
		var homeCoords = [];
		homeCoords.push(routeObj[0]);

		//calc time of journey
		var finalTime = new Date(routeObj[routeObj.length - 1].time);
		var initialTime = new Date(routeObj[0].time);

		var timeOfJourney = Math.round((finalTime - initialTime)  / 60000); // minutes

		var stayAtHomeTime = numOfMinsAtWork + (2 * timeOfJourney);
		Routes.calcindexforgoingtowork(homeCoords, stayAtHomeTime, function(err, indexResult){
			cb(null, indexResult);
		})
	}

	Routes.calcindexforgoingtowork = function(routeObj, numOfMins, cb){
			var index = 0;
			var avgindex = 0;
			var lengthForAvgIndex = 0;
			var numOfMinsCopy = numOfMins;
			var routeObjCopy = JSON.parse(JSON.stringify(routeObj));

			//outwards trip
			Routes.calcindex(routeObj, function(err, indexResult){
				console.log("indexresult: "+indexResult.pollutionIndex)
				index += indexResult.pollutionIndex;

				lengthForAvgIndex += indexResult.numOfLocations;

				//{"lat":51.5228749,"lng":-0.1315501,"time":"2015-12-3 10:29:00"}..
			
				//stay at work calc
				var destinationObject = routeObj[routeObj.length-1];
				var destLat = destinationObject.lat;
				var destLng = destinationObject.lng;
				var absoluteTimeAtDest = new Date(destinationObject.time);
				
				var absoluteTimeAtDestMomentObj = moment(absoluteTimeAtDest);

				var a = 0;
				var stayRoute = [{}];
				stayRoute[0].lat = destLat;
				stayRoute[0].lng = destLng;

			


				async.whilst(
				    function () { return numOfMins > 0; },
				    function (callback) {

				    	if(absoluteTimeAtDestMomentObj.get('minute') == 30 && numOfMins>=60){ //optimisation


				    		absoluteTimeAtDestMomentObj.add(59, 'minutes');

							var absoluteTimeAtDestString = absoluteTimeAtDestMomentObj.format("YYYY-MM-DD HH:mm:ss");

							
							stayRoute[0].time = absoluteTimeAtDestString;
							
							console.log("minute 0: "+absoluteTimeAtDestString);


							Routes.calcindex(stayRoute, function(err, indexResult){
								console.log("stayRoute below");
								console.log(stayRoute);
								//console.log("indexresult: "+indexResult)
								console.log("returnindexresult: "+index+ "numLocs: "+lengthForAvgIndex);

								index += indexResult.averagePollutionIndex*59;
								lengthForAvgIndex += 59;
								console.log("returnindexresult: "+index+ "numLocs: "+lengthForAvgIndex);


								numOfMins -= 59;
								callback(null,numOfMins);
							});

				    	}else{ //normal calc for individual minutes


					        absoluteTimeAtDestMomentObj.add(1, 'minutes');

							var absoluteTimeAtDestString = absoluteTimeAtDestMomentObj.format("YYYY-MM-DD HH:mm:ss");

							
							stayRoute[0].time = absoluteTimeAtDestString;
							
							console.log(stayRoute);


							Routes.calcindex(stayRoute, function(err, indexResult){
								console.log("returnindexresult: "+index+ "numLocs: "+lengthForAvgIndex);
								index += indexResult.pollutionIndex;
								lengthForAvgIndex += indexResult.numOfLocations;
																console.log("returnindexresult: "+index+ "numLocs: "+lengthForAvgIndex);


								numOfMins--;
								callback(null,numOfMins);
							});
						}

				    },
				    function (err) {
				    	if(err){
				    		console.log("err: "+err);
				    		cb(err,-1);
				    	}else{
				    		console.log(index);


				    		//calc inbound trip index!

				    		var numOfCoords = routeObjCopy.length;
							var initialTime = new Date(routeObjCopy[0].time);
							var finalTime = new Date (routeObjCopy[numOfCoords-1].time);

							var diffMs = finalTime - initialTime;
							var diffMins = Math.round(diffMs  / 60000); // minutes

							


							

							var routeObjReversed = JSON.parse(JSON.stringify(routeObjCopy));


				    		routeObjReversed = routeObjReversed.reverse();

				    		//console.log(routeObjCopy);
				    		//console.log(routeObjReversed);


							for(coord in routeObjCopy){
							   routeObjCopy[coord].lng = routeObjReversed[coord].lng;
							   routeObjCopy[coord].lat = routeObjReversed[coord].lat;
							   routeObjCopy[coord].time = (moment(new Date(routeObjCopy[coord].time)).add(diffMins+numOfMinsCopy, 'minutes')).format("YYYY-MM-DD HH:mm:ss");
							}
							//console.log("return route object PRINT:" +routeObjCopy);
							//console.log(routeObjCopy);
							//							console.log("return route object PRINT:" +routeObjCopy);


				    		Routes.calcindex(routeObjCopy, function(err, indexResult){
								//console.log("returnindexresult: "+index+ "numLocs: "+lengthForAvgIndex);
								index += indexResult.pollutionIndex;
								lengthForAvgIndex += indexResult.numOfLocations;
																//console.log("returnindexresult: "+index+ "numLocs: "+lengthForAvgIndex);


								var returnPollutionIndex = {
									"pollutionIndex" : index,
									"averagePollutionIndex" : index/lengthForAvgIndex,
									"numOfLocations" : lengthForAvgIndex
								};
								
								cb(null, returnPollutionIndex);
							});





				    		
				    	}
				        //callback(err,n);
				    }
				);


			});



					
	}

	Routes.calcindexgivenid = function(id, cb){

		Routes.findOne({where: {id: id}}, function(err, routeObj) { 
			Routes.calcindex(routeObj.route, function(err, indexResult){
				cb(null,indexResult);
			});
		});
	}

	Routes.calcindex = function(routeobject, cb){



		/*function addMinutes(date, minutes) {
    		return new Date(date.getTime() + minutes*60000);
		}

		function calculatePollutionAtPoint(coordinate, date){ //look at closest sensor compared to current coordinate and pull the pollution value.
			return Math.random()*10;
		}

		function calculateIndexAtPoint(coordinate, date){
			//



			//calc nearest hour
			date.setHours(date.getHours() + Math.round(date.getMinutes()/60));
			date.setMinutes(0);
			date = moment(date).format("YYYY-MM-DD HH:mm:ss");


			//lookup in the pollutions table first


			Pollution.find({where: {date: date}}, function(err, pollutions) { 
				var output = [];
				for (pollution in pollutions){
					var dateUserIDArray = pollutions[pollution].dateuserid.split(',');
					var userid = Number(dateUserIDArray[1]);
					if(userid == id){
						output.push(feedbacks[feedback]);
					}
				}
				cb(null, output);
		 	});

			//if lookup not in pollutions table check in predictions table

			//if not found return error

	
			var pollution = calculatePollutionAtPoint(coordinate, date);
		




			return indexFromPollution(pollution);
	
		}*/

		//Normalise the index to a percentage rating???? 
		//Minimum solution: Do nothing. Will normalise on client.

		//function normaliseIndex(index)


		//Generate an index from a single pollution value
		//Suggest investigating this link: http://airnow.gov/index.cfm?action=resources.conc_aqi_calc
		//Minimum Solution: return un-modified pollution level.
		 
		//function indexFromPollution(pollution)


		// Given the GPS point determine the pollution level. 
		// An ideal solution will combine several of the nearest sensor readings and calculate 
		// the 'between-hour' values using a weighted average between the previous hour and the upcoming hour. 
		// Minimum Solution: locate the nearest sensor, 
		// take the reading for the closest hour to the date and return its value.




		

			
			// feed in only minutes values
			
				


			var route = [];
			



			/*route.push({"lat":51.5228749,"lng":-0.1315501,"time":"2015-10-11 09:29:00"});
			route.push({"lat":51.5227949,"lng":-0.1317424,"time":"2015-10-11 09:32:00"});
			route.push({"lat":51.5224498,"lng":-0.1326473,"time":"2015-10-11 09:33:00"});
			route.push({"lat":51.52717579999999,"lng":-0.1386533,"time":"2015-10-11 09:34:00"});
			route.push({"lat":51.5271522,"lng":-0.1408669,"time":"2015-10-11 09:35:00"});
			route.push({"lat":51.5272961,"lng":-0.1408564,"time":"2015-10-11 09:36:00"});*/

		async.series([

			function(callback){


				var initroute = routeobject;
				console.log(initroute.length);

				var numOfCoords = initroute.length;
				var initialTime = new Date(initroute[0].time);
				var finalTime = new Date (initroute[numOfCoords-1].time);

				var diffMs = finalTime - initialTime;
				var diffMins = Math.round(diffMs  / 60000); // minutes
				console.log("diffmins: "+diffMins);

				//if we have 10000 coords for 10 mins we need coords for every minute so take every 10000/100 cords

				var x = 0;
				if(initroute.length == 1){
					route.push(initroute[0]);
				}
				while(x<initroute.length){
					route.push(initroute[x]);
					if(numOfCoords/diffMins < 1){
						x+= 1;
					}else{
						x += parseInt(numOfCoords/diffMins);
					}
				}

				callback(null,0);


			},function(callback){

				Routes.app.models.Site.find({where: { }}, function(err, sitesList) { 


					for (step in route){
						var userLat = route[step].lat* Math.PI / 180;
						var userLng = route[step].lng* Math.PI / 180;


						var mindist = Number.MAX_VALUE;
						var mindistSite = 0;
						for(site in sitesList){
							//console.log(sitesList[site]);
							var siteLat = sitesList[site].lat* Math.PI / 180;
							var siteLng = sitesList[site].lng* Math.PI / 180;

							var R = 6371; // km
							var dLat = siteLat-userLat;
							var dLon = siteLng-userLng;

							var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
							Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(userLat) * Math.cos(siteLat); 
							var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
							var d = R * c;

							if(d<mindist){
								mindist=d;
								mindistSite=sitesList[site].siteId;
							}

						}
						console.log(userLat + ", " + userLng + ", " + mindistSite+ ", "+mindist + ", " +route[step].time);

						
						//get nearest hour 
						var dateTimeOfStep = new Date(route[step].time);
						dateTimeOfStep.setHours(dateTimeOfStep.getHours() + Math.round(dateTimeOfStep.getMinutes()/60));
	    				dateTimeOfStep.setMinutes(0);
	    				dateTimeOfStep.setSeconds(0);
	    				var dateTimeOfStepString = moment(dateTimeOfStep).format("YYYY-MM-DD HH:mm:ss");
	    				//console.log(dateTimeOfStepString);
	    				route[step].closestSiteId = mindistSite;
	    				route[step].time = dateTimeOfStepString;

	    				//console.log(route);

	    			}
	    			callback(null,0);

	    			
	    		});
					//console.log(route);
	    			//cb(null,0);
	    			//return 0;

			},function(callback){
				
				var pollutionIndex = getPollutionIndex(route);

				


			}


		]);

		function Linear(AQIhigh, AQIlow, Conchigh, Conclow, Concentration) {
		    var linear;
		    var Conc = parseFloat(Concentration);
		    var a;
		    a = ((Conc - Conclow) / (Conchigh - Conclow)) * (AQIhigh - AQIlow) + AQIlow;
		    linear = a;
		    //linear = Math.round(a);
		    return linear;
		}

		function AQIPM25(Concentration) {
		    var Conc = parseFloat(Concentration);
		    var c;
		    var AQI;
		    c = (Math.floor(10 * Conc)) / 10;

		    if (c >= 0 && c < 36) {
		        AQI = Linear(3.9, 1, 36, 0, c);

		    } else if (c >= 36 && c < 42) {
		        AQI = Linear(4.9, 4, 42, 36.1, c);

		    } else if (c >= 42 && c < 48) {
		        AQI = Linear(5.9, 5, 48, 42.1, c);

		    } else if (c >= 48 && c < 54) {
		        AQI = Linear(6.9, 6, 54, 48.1, c);

		    } else if (c >= 54 && c < 59) {
		        AQI = Linear(7.9, 7, 59, 54.1, c);

		    } else if (c >= 59 && c < 65) {
		        AQI = Linear(8.9, 8, 65, 59.1, c);

		    } else if (c >= 65 && c < 71) {
		        AQI = Linear(9.9, 9, 71, 65.1, c);

		    } else if (c >= 71) {
		        AQI = 10;

		    } else {
		        AQI = "Out of Range";
		    }
		    return AQI;
		}

		function AQIPM10(Concentration) {
		    var Conc = parseFloat(Concentration);
		    var c;
		    var AQI;
		    c = Math.floor(Conc);
		    if (c >= 0 && c < 51) {
		        AQI = Linear(3.9, 1, 50, 0, c);

		    } else if (c >= 51 && c < 101) {
		        AQI = Linear(9.9, 4, 100, 51.1, c);

		    } else if (c >= 101) {
		        AQI = 10;

		    } else {
		        AQI = "Out of Range";
		    }
		    return AQI;
		}

		function AQINO2(Concentration) {
		    var Conc = parseFloat(Concentration); //in ug/(m^3) , need to convert into ppb
		    var divisor = (1.9125 + 1.88)/2;
		    var oneMGoverM3 = 1/divisor; // ppb
		    Conc = Conc * oneMGoverM3;
		    var c;
		    var AQI;
		    c = (Math.floor(Conc)) / 1000;
		    if (c >= 0 && c < 601) {
		        AQI = Linear(9.9, 1, 601, 0, c);

		    } else if (c >= 601) {
		        AQI = 10;

		    } else {
		        AQI = "Out of Range";
		    }
		    return AQI;
		}

		function normaliseIndex(pollutant,concentrationVal){
			if(pollutant == "NO2"){
				return AQINO2(concentrationVal);
			}else if (pollutant == "PM25"){
				return AQIPM25(concentrationVal);
			}else if (pollutant == "PM10"){
				return AQIPM10(concentrationVal);
			}else{
				//unknown 
				console.log("unknown pollutant");
				return concentrationVal;
			}
		}

		function getPollutionIndex(route){
			var pollutionVal = 0;
			var numberOfMissingPollutions = 0;
			async.forEachOf(route, function(step, key, callback){
				console.log(step);
					Routes.app.models.Pollution.find({where: { siteId : step.closestSiteId , date : step.time}}, function(err, pollutionItems) {
						console.log("Pollution query results: "+pollutionItems);

			    					if(err) {
			    						console.log(err);
			    						callback(err);

			    					}
			    					console.log("print is from pollution: "+ pollutionItems)
			    					if(pollutionItems && !err){
			    						if(pollutionItems.length > 0){
			    							var pollutionAvg = 0;
				    						for(pollutionItem in pollutionItems){
				    							//pollutionVals.push({'species_id':pollutionItems[pollutionItem].speciesId,'value':pollutionItems[pollutionItem].value});
				    							//NORMASE HERE FIRST
				    							var normalisedIndex = normaliseIndex(pollutionItems[pollutionItem].speciesId,pollutionItems[pollutionItem].value);
				    							pollutionAvg += normalisedIndex;
				    							console.log("pollutionind: "+normalisedIndex);
				    						}
				    						pollutionAvg /= pollutionItems.length;
				    						console.log("pollutionAvg: "+pollutionAvg);

				    						//route[key].pollutionVal = pollutionVals[0].value;
				    						route[key].pollutionVal = pollutionAvg;
				    						//final add averaged and normalised pollution val to route[key]
				    						callback(null,0);


				    					}else{
				    						Routes.app.models.Prediction.find({where: { siteId : step.closestSiteId , date : step.time}}, function(err1, predictionItems) {
				    							console.log("Prediction query results: "+predictionItems);
				    							if(err1) {
						    						console.log(err1);
						    						callback(err1);
						    						return;

						    					}
						    					console.log("print is from prediction: "+ predictionItems + ", len: "+predictionItems.length)

						    					if(predictionItems && !err1){
						    						if(predictionItems.length > 0){
						    							console.log("in here");
						    							var predictionAvg = 0;
						    							var numOfPredictionItems = 0;
						    							for(predictionItem in predictionItems){
						    								//predictionVals.push({'species_id':predictionVals[predictionItem].species_id,'value':predictionVals[predictionItem].value});
						    								
						    								if(predictionItems[predictionItem].value != -1){
						    									console.log("predictionindunnormalised: "+predictionItems[predictionItem].speciesId + ", "+ predictionItems[predictionItem].value);
						    									numOfPredictionItems++;
							    								var normalisedIndex = normaliseIndex(predictionItems[predictionItem].speciesId,predictionItems[predictionItem].value);
							    								predictionAvg += normalisedIndex;
							    								console.log("predictionind: "+normalisedIndex);
						    								}
						    								

						    							}
						    							predictionAvg /= numOfPredictionItems;

						    							if(pollutionAvg == 0){
							    							if(key!=0){
								    							if(route[key-1]){
								    								if(route[key-1].pollutionVal > 0){
								    									route[key].pollutionVal = pollutionVal;
								    								}else{
								    									route[key].pollutionVal = 0;
								    								}
								    							}else{
								    								route[key].pollutionVal = 0;
								    							}
							    							}else{
							    								//estimated average pollution standard...?
							    								route[key].pollutionVal = 0;
							    								numberOfMissingPollutions++;
							    							}
						    							}else{

							    							console.log("predictionAvg: "+predictionAvg);

							    							//route[key].pollutionVal = predictionVals[0].value;
							    							route[key].pollutionVal = predictionAvg;
						    							}
						    							callback(null,0);

						    						}else{
						    							console.log("in here2");

						    							//negative prediction...?
						    							//check previous value if exists and greater than 0 then use otherwise make 0
						    							if(key!=0){
							    							if(route[key-1]){
							    								if(route[key-1].pollutionVal > 0){
							    									route[key].pollutionVal = pollutionVal;
							    								}else{
							    									route[key].pollutionVal = 0;
							    								}
							    							}else{
							    								route[key].pollutionVal = 0;
							    							}
						    							}else{
						    								//estimated average pollution standard...?
						    								route[key].pollutionVal = 0;
						    								numberOfMissingPollutions++;
						    							}
						    							callback(null,0);
						    						}
						    					}else{
						    						console.log("no results/err")


						    						callback(pollutionItems,-1);
						    						return;
						    					}
				    						});

				    					}
			    						
			    					}else{
			    						console.log("empty detect");
			    						//check if it exists in prediction table
			    						


			    					}
		    						//console.log(err);
		    						
		    		});
			}, function(err){

				if(err){
					console("err: "+err);
				}else{
					console.log(route);
					var pollutionIndex = 0;
					for(step in route){
						console.log(route[step].pollutionVal)
						pollutionIndex += route[step].pollutionVal;
					}
					console.log(pollutionIndex);
					//pollutionIndex += (pollutionIndex/(route.length-numberOfMissingPollutions)) * numberOfMissingPollutions; //to account for missing pollutions (doesnt matter anymore as we are doing an average)
					var avgPollutionIndex = pollutionIndex / route.length;
					console.log("averagepolllutionindexreturned" +avgPollutionIndex);
					var returnPollutionIndex = {
						"pollutionIndex" : pollutionIndex,
						"averagePollutionIndex" : avgPollutionIndex,
						"numOfLocations" : route.length
					};
					cb(null,returnPollutionIndex);
				}
			});

					




				

			
					
		}
    	
    	//genClosestSitesofRoute().then(function(route){
			//continue
			/*Routes.app.models.Pollution.findOne({where: { siteId : mindistSite , date : dateTimeOfStepString}}, function(err, pollutionItem) {
    					if(err) return -1;
    					if(pollutionItem && !err){
    						console.log(userLat + ", " + userLng + ", " + mindistSite+ ", "+mindist + ", " +route[step].time +", " + pollutionItem.value);
    					}else{
    						//check in prediction table
    					}
    					//console.log(err);
    				});*/

		//}, function(error){
			//handle errors
		//});

    	
				



				    //routeObject = JSON.parse(route);

			



				
			



			

			


			/*for (step in route){
				var lat = route[step].end_location.lat;
				var lng = route[step].end_location.lng;

			}*/



			/*var index = 0;
			var date = new Date(des);

			//whole journey88

			//process only routes on every 60 seconds / 1 minute



			for (coordinate in route) {
				date = addMinutes(date, 1);// assuming that every new coord is a min after
				index += calculateIndexAtPoint(route[coordinate], date);
			}

			//Time at work

			var destinationPoint = route[route.length -1];

			var x = 0;
			for(x;x<timeAtDest;x++){
				date = addMinutes(date, 1);

				index += calculateIndexAtPoint(destinationPoint, date);

				// Possible extra step here:
				// Reduce index slightly to account for being inside??? Ignore for now.			

			}

			//Return journey

			for (coordinate in route.reverse) {
				date = addMinutes(date, 1);
				index += calculateIndexAtPoint(coordinate, date);
			}

					//This step might not be necessary depending on 'indexFromPollution' implementation.

			//index = normaliseIndex(index);
		
			return index;
			*/

		




		//[[lat,lng],[lat,lng]]

		/*Feedback.find({where: { }}, function(err, feedbacks) { 
			var output = [];
			for (feedback in feedbacks){
				var dateUserIDArray = feedbacks[feedback].dateuserid.split(',');
				var userid = Number(dateUserIDArray[1]);
				if(userid == id){
					output.push(feedbacks[feedback]);
				}
			}
			cb(null, output);
		 });*/
	}
	
	Routes.remoteMethod (
		'predictIndexGivenRouteID',
		{
			accepts: [{arg: 'routeid',type:'number'},{arg: 'numOfMinsStayTimeAtWork',type:'number'},{arg: 'startdatetimeforprediction',type:'string'}],
			returns: {arg: 'pollutionExposureIndex', type:'object'}
		}
	);
	Routes.remoteMethod (
		'calcIndexGivenStayTime',
		{
			accepts: [{arg: 'routeobject', type:'object'},{arg: 'staytimeatworkinminutes', type:'number'}],
			returns: {arg: 'pollutionExposureIndex', type:'object'}
		}
	);
	Routes.remoteMethod (
		'calcindex',
		{
			accepts: {arg: 'routeobject', type:'object'},
			returns: {arg: 'pollutionExposureIndex', type:'object'}
		}
	);
	Routes.remoteMethod (
		'calcindexgivenid',
		{
			accepts: {arg: 'routeid', type:'number'},
			returns: {arg: 'pollutionExposureIndex', type:'object'}
		}
	);
};
