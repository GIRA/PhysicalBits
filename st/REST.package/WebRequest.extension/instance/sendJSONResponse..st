*REST-Core
sendJSONResponse: aString
	self
		send200Response: (WebUtils jsonEncode: aString)
		contentType: 'application/json; charset=utf-8'