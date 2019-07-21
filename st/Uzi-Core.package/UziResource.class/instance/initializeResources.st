initialize-release
initializeResources 
	self at: #pins put: (UziPinResource server: server);
		at: #globals put: (UziGlobalResource server: server);
		at: #program put: (UziProgramResource server: server);
		at: #debugger put: (UziDebuggerResource server: server);
		at: #actions 
		put: (RESTActionMap new
			at: #compile do: [:req | self processCompile: req];
			at: #connect do: [:req | self processConnect: req]
			ifTrue: [self controller isConnected not];
			at: #disconnect do: [:req | self processDisconnect: req]
			ifTrue: [self controller isConnected];
			at: #install do: [:req | self processInstall: req]
			ifTrue: [self controller isConnected];
			at: #run do: [:req | self processRun: req]
			ifTrue: [self controller isConnected];
			at: #pinReport do: [:req | self processPinReport: req];
			yourself)