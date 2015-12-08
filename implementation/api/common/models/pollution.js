module.exports = function(Pollution) {
	Pollution.overallAvgForDateTime = function(dateTime, cb){

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

			var avgPM10 = 0, avgPM25 = 0, avgNO2 = 0;

			for(val in sensorVals.PM10){
				//console.log(avgPM10);
				avgPM10 += sensorVals.PM10[val];
			}
			avgPM10 /= sensorVals.PM10.length;

			for(val in sensorVals.PM25){
				avgPM25 += sensorVals.PM25[val];
			}
			avgPM10 /= sensorVals.PM25.length;

			for(val in sensorVals.NO2){
				avgNO2 += sensorVals.NO2[val];
			}
			avgNO2 /= sensorVals.NO2.length;

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
