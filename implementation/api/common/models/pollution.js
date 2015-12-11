var moment = require('moment');
module.exports = function(Pollution) {
	Pollution.overallAvgForDateTime = function(dateTime, cb){

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

		var tempDateTime = new Date(dateTime);
						tempDateTime.setHours(tempDateTime.getHours() + Math.round(tempDateTime.getMinutes()/60));
	    				tempDateTime.setMinutes(0);
	    				tempDateTime.setSeconds(0);
	    				dateTime = moment(tempDateTime).format("YYYY-MM-DD HH:mm:ss");

		function findAvg(dateTime, pollutionItems){		

			var sensorVals = {
				'PM10' : [],
				'PM25' : [],
				'NO2' : []
			}

			for(item in pollutionItems){
				var speciesId = pollutionItems[item].speciesId;
				var value = pollutionItems[item].value;

				if(speciesId == "PM10" && value != -1){
					sensorVals.PM10.push(value);
				}else if(speciesId == "PM25" && value != -1){
					sensorVals.PM25.push(value);
				}else if(speciesId == "NO2" && value != -1){
					sensorVals.NO2.push(value);
				}
				//console.log(sensorVals);

			}

			var avgPM10 = 0.0, avgPM25 = 0.0, avgNO2 = 0.0;

			for(val in sensorVals.PM10){
				//console.log(avgPM10);
				avgPM10 += sensorVals.PM10[val];
			}
			avgPM10 /= sensorVals.PM10.length;
			avgPM10 = normaliseIndex("PM10",avgPM10);

			for(val in sensorVals.PM25){
				avgPM25 += sensorVals.PM25[val];
			}
			avgPM25 /= sensorVals.PM25.length;
			avgPM25 = normaliseIndex("PM25",avgPM25);

			for(val in sensorVals.NO2){
				avgNO2 += sensorVals.NO2[val];
			}
			avgNO2 /= sensorVals.NO2.length;
			avgNO2 = normaliseIndex("NO2",avgNO2);

			var overallAvg = (avgPM10 + avgPM25 + avgNO2) / 3;

			if(overallAvg){
				return overallAvg;
			}else{
				return -1;
			}
		}

			Pollution.find({where: { date : dateTime}}, function(err, pollutionItems) {
				var result = findAvg(dateTime, pollutionItems);
				if(result != -1){
					cb(null, result);
				}else{
					//check predictions table
					Pollution.app.models.Prediction.find({where: { date : dateTime}}, function(err, predictionItems) {

						var result = findAvg(dateTime, predictionItems);
						if(result != -1){
							cb(null, result);
						}else{
							cb(-1,"not found");
						}
					});

				}
			});
		

	}
	Pollution.remoteMethod (
		'overallAvgForDateTime',
		{
			accepts: {arg: 'datetime',type:'string'},
			returns: {arg: 'avg', type:'number'}
		}
	);
};
