var moment = require('moment');
var request = require('request');
var async = require('async');
module.exports = function(Routes) {

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
				console.log(diffMins);

				//if we have 10000 coords for 10 mins we need coords for every minute so take every 10000/100 cords

				var x = 0;
				while(x<initroute.length-1){
					route.push(initroute[x]);
					if(numOfCoords/diffMins < 1){
						x+= 1;
					}else{
						x += numOfCoords/diffMins;
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

		function getPollutionIndex(route){
			var pollutionVal = 0;
			async.forEachOf(route, function(step, key, callback){
				console.log(step);
					Routes.app.models.Pollution.findOne({where: { siteId : step.closestSiteId , date : step.time}}, function(err, pollutionItem) {
						console.log(pollutionItem);

			    					if(err) {
			    						console.log(err);
			    						callback(err);

			    					}
			    					if(pollutionItem && !err){
			    						pollutionVal = pollutionItem.value;
			    						route[key].pollutionVal = pollutionVal;
			    						callback(null,0);
			    					}else{
			    						console.log("empty detect");
			    						//check if it exists in prediction table
			    						Routes.app.models.Prediction.findOne({where: { siteId : step.closestSiteId , date : step.time}}, function(err1, pollutionItem1) {
			    							console.log(pollutionItem1);
			    							if(err1) {
					    						console.log(err1);
					    						callback(err1);

					    					}
					    					if(pollutionItem1 && !err1){
					    						pollutionVal = pollutionItem1.value;
					    						route[key].pollutionVal = pollutionVal;
					    						callback(null,0);
					    					}else{
					    						console.log("no results")


					    						callback(pollutionItem,-1);
					    						return;
					    						//check in prediction table
					    					}
			    						});



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
						pollutionIndex += route[step].pollutionVal;
					}
					cb(null,pollutionIndex);
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
		'calcindex',
		{
			accepts: {arg: 'routeobject', type:'object'},
			returns: {arg: 'pollutionExposureIndex', type:'number'}
		}
	);

};
