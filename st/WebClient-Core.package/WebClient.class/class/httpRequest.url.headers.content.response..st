httpsocket
httpRequest: method url: urlString headers: hdrs content: contentOrNil response: responseBlock

	"Sends an HTTP request to the server. Returns a MIMEDocument if successful,
	a string indicating the error otherwise. If a response block is provided, the
	response is fed into into so that the sender can see all the headers."

	| client request response content |
	client := self new.
	client initializeFromUrl: urlString.
	client userAgent: HTTPSocket userAgentString.

	request := client requestWithUrl: urlString.
	request method: method.

	"Set up headers for the request"
	(WebUtils readHeadersFrom: hdrs readStream) 
		do:[:hdr| request addHeader: hdr key value: hdr value].

	WebUtils handleAuth:[
		response := client sendRequest: request 
			content: (contentOrNil ifNotNil:[contentOrNil readStream])
			size: (contentOrNil ifNil:[0] ifNotNil:[contentOrNil size]).
	].

	content := response contentWithProgress:[:total :amount|
		(HTTPProgress new) total: total; amount: amount; signal: 'Downloading...'.
	].
	client close.

	responseBlock ifNotNil:[responseBlock value: response asString].

	^(response isSuccess) 
		ifTrue:[MIMEDocument contentType: response contentType 
				content: content url: urlString]
		ifFalse:[response asString, content].
