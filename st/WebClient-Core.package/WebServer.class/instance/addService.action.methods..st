services
addService: entryPoint action: aBlockOrMessage methods: methods
	"Add a service to the web server. The service is specified by an entryPoint, 
	denoting the url under which the service can be requested 
	(such as /my/great/service) and an action which is a message 
	send taking a single argument, the WebRequest.
	
	The list of methods describes what HTTP methods the given url supports."

	mutex critical:[
		entryPoints at: entryPoint asLowercase put: {methods. aBlockOrMessage}.
	].