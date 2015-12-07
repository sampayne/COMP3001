module.exports = function(Feedback) {
	Feedback.generalavg = function(id, cb){
		Feedback.find({where: { }}, function(err, feedbacks) { 
			var avg = 0;
			var length = 0;
			for (feedback in feedbacks){
				var dateUserIDArray = feedbacks[feedback].dateuserid.split(',');
				var date = dateUserIDArray[0];
				var userid = Number(dateUserIDArray[1]);
				if(userid == id){
					avg += feedbacks[feedback].rating;
					length++;
				}
			}
			avg /= length;
			cb(null, avg);
		 });
	}
	Feedback.thirtydayavg = function(id, cb) {
		var ONE_MONTH = 30 * 24 * 60 * 60 * 1000;  // Month in milliseconds
		var avg = 0;
		var length = 0;
		Feedback.find({where: { }}, function(err, feedbacks) { 
			for (feedback in feedbacks){
				var dateUserIDArray = feedbacks[feedback].dateuserid.split(',');
				var date = dateUserIDArray[0];
				var userid = Number(dateUserIDArray[1]);
				if(userid == id && new Date(date) > Date.now() - ONE_MONTH){
					avg += feedbacks[feedback].rating;
					length++;
				}
			}
			avg /= length;
			cb(null, avg);
		 });
	}
	Feedback.sevendayavg = function(id, cb) {
		var ONE_WEEK = 7 * 24 * 60 * 60 * 1000;  // Month in milliseconds
		var avg = 0;
		var length = 0;
		Feedback.find({where: { }}, function(err, feedbacks) { 
			for (feedback in feedbacks){
				var dateUserIDArray = feedbacks[feedback].dateuserid.split(',');
				var date = dateUserIDArray[0];
				var userid = Number(dateUserIDArray[1]);
				if(userid == id && new Date(date) > Date.now() - ONE_WEEK){
					avg += feedbacks[feedback].rating;
					length++;
				}
			}
			avg /= length;
			cb(null, avg);
		 });
	}

	Feedback.getbyuserid = function(id, cb){
		Feedback.find({where: { }}, function(err, feedbacks) { 
			var output = [];
			for (feedback in feedbacks){
				var dateUserIDArray = feedbacks[feedback].dateuserid.split(',');
				var userid = Number(dateUserIDArray[1]);
				if(userid == id){
					output.push(feedbacks[feedback]);
				}
			}
			var returnJSON = {
				'feedbacks': output,
				'avg': 0
			};
			Feedback.generalavg(id, function(err, avg){
				returnJSON.avg = avg;
				cb(null, returnJSON);
			})
		 });
	}

	Feedback.remoteMethod (
		'generalavg',
		{
			accepts: {arg: 'userid', type:'number'},
			returns: {arg: 'avg'}
		}
	)
	Feedback.remoteMethod (
		'thirtydayavg',
		{
			accepts: {arg: 'userid', type:'number'},
			returns: {arg: 'avg'}
		}
	);
	Feedback.remoteMethod (
		'sevendayavg',
		{
			accepts: {arg: 'userid', type:'number'},
			returns: {arg: 'avg'}
		}
	);
	Feedback.remoteMethod (
		'getbyuserid',
		{
			accepts: {arg: 'userid', type:'number'},
			returns: {arg: 'feedback'}
		}
	);

};
