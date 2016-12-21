logging
logRequest: request response: response
	"Log a request after it has been successfully processed"

	accessLog ifNil:[^self].

	"Do not use the standard exception handler for errors logging; 
	it would possibly send a 500 response. Really this should be handled
	by a different process with its own error handler."

	[WebUtils logRequest: request response: response on: accessLog]
		on: Error do:[:ex| self log: 'Error in logging: ', ex description].
