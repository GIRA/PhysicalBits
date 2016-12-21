accessing
errorHandler: aBlock
	"The user-supplied error handler. If present the handler takes two arguments:
		* The error being caught by WebServer
		* The request the error occurred in.
	The user-supplied error handler can do whatever it wants, including calling
	WebServer's #handleError:request: method which is the default."

	errorHandler := aBlock.