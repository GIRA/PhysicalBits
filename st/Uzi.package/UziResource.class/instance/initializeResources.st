initialize-release
initializeResources 
	self at: #actions 
		put: (RESTActionMap new
			at: #compile do: [:req | self processCompile: req];
			at: #connect do: [:req | self processConnect: req]
			ifTrue: [protocol isConnected not];
			at: #disconnect do: [:req | self processDisconnect: req]
			ifTrue: [protocol isConnected];
			at: #install do: [:req | self processInstall: req]
			ifTrue: [protocol isConnected];
			at: #run do: [:req | self processRun: req]
			ifTrue: [protocol isConnected];
			yourself)