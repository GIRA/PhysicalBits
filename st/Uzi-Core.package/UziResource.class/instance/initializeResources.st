initialize-release
initializeResources 
	self at: #actions 
		put: (RESTActionMap new
			at: #compile do: [:req | self processCompile: req];
			at: #connect do: [:req | self processConnect: req]
			ifTrue: [self protocol isConnected not];
			at: #disconnect do: [:req | self processDisconnect: req]
			ifTrue: [self protocol isConnected];
			at: #install do: [:req | self processInstall: req]
			ifTrue: [self protocol isConnected];
			at: #run do: [:req | self processRun: req]
			ifTrue: [self protocol isConnected];
			yourself);
		at: #pins put: (UziPinResource server: server);
		at: #globals put: (UziGlobalResource server: server)