examples
exampleDoIt		"WebServer exampleDoIt"
	"This example executes doIts from a POST request.
	It's the simplest possible RPC interface to Squeak."

	| server port |
	port := 9999.
	server := WebServer reset default.
	server listenOn: port.
	server addService: '/' action:[:req| 
		| data result |
		req isPostRequest ifTrue:[
			"We fetch all of the data instead of streaming it"
			data := req content.
			"Compute the result"
			result := [Compiler evaluate: data] on: Error do:[:ex| ex return: ex asString].
			req send200Response: result asString 
		] ifFalse:[req send404Response].
	].
