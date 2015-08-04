initialize-release
initializeResources
	self at: #web put: (RESTFileSystem directory: webDirectory);
		at: #uzi put: (RESTActionMap new
			at: #connect do: [:req | self processConnect: req]
			ifTrue: [protocol isConnected not];
			at: #disconnect do: [:req | self processDisconnect: req]
			ifTrue: [protocol isConnected];			
			at: #compile do: [:req | self processCompile: req];
			at: #install do: [:req | self processInstall: req]
			ifTrue: [protocol isConnected];
			at: #run do: [:req | self processRun: req]
			ifTrue: [protocol isConnected];
			yourself)