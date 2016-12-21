services
removeService: entryPoint 
	"Remove a service from the HTTP service provider.  See #addService:action:"

	mutex critical:[
		entryPoints removeKey: entryPoint asLowercase ifAbsent: [].
	].