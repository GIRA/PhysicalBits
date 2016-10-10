initialize-release
initialize
	super initialize.
	self initializeResources;
		onGET: [:req | req send302Response: '/web/index.html']