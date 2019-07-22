private
send200ResponseFor: req
	req send200Response: 'OK'
		contentType: 'text/plain; charset=utf-8'
		do: [:resp | resp headerAt: 'Access-Control-Allow-Origin' put: '*']