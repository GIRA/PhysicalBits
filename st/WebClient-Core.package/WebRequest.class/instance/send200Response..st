responses
send200Response: aString
	"Send a 200 OK response"

	^self 
		send200Response: (aString convertToWithConverter: UTF8TextConverter new) 
		contentType: 'text/plain; charset=utf-8'.