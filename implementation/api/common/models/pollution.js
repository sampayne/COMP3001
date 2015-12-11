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
		    if (c >= 0 && c < 12.1) {
		        AQI = Linear(3.9, 1, 12, 0, c);
		    } else if (c >= 12.1 && c < 35.5) {
		        AQI = Linear(6.9, 4, 35.4, 12.1, c);
		    } else if (c >= 35.5 && c < 150.5) {
		        AQI = Linear(9.9, 7, 150.4, 35.5, c);
		    } else if (c >= 150.5 && c < 500.5) {
		        AQI = Linear(10, 10, 500.4, 150.5, c);
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
		    if (c >= 0 && c < 55) {
		        AQI = Linear(3.9, 1, 54, 0, c);
		    } else if (c >= 55 && c < 155) {
		        AQI = Linear(6.9, 4, 154, 55, c);
		    } else if (c >= 155 && c < 355) {
		        AQI = Linear(9.9, 7, 354, 155, c);
		    } else if (c >= 355 && c < 605) {
		        AQI = Linear(10, 10, 604, 355, c);
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
		    if (c >= 0 && c < .054) {
		        AQI = Linear(3.9, 1, .053, 0, c);
		    } else if (c >= .054 && c < .101) {
		        AQI = Linear(6.9, 4, .100, .054, c);
		    } else if (c >= .101 && c < .650) {
		        AQI = Linear(9.9, 7, .649, .101, c);
		    } else if (c >= .650 && c < 2.049) {
		        AQI = Linear(10, 10, 2.049, .650, c);
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
