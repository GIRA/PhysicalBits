private
sendJSONResponse: aString	for: req
	req send200Response: (WebUtils jsonEncode: aString)
		contentType: 'application/json; charset=utf-8'
		do: [:resp | resp headerAt: 'Access-Control-Allow-Origin' put: '*']