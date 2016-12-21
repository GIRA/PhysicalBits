services
addService: entryPoint action: aBlockOrMessage
	"Add a service to the web server. The service is specified by an entryPoint, 
	denoting the url under which the service can be requested 
	(such as /my/great/service) and an action which is a message 
	send taking a single argument, the WebRequest."

	^self addService: entryPoint 
		action: aBlockOrMessage 
		methods: self defaultHttpMethods.