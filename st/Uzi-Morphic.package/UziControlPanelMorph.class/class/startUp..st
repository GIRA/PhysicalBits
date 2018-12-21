system startup
startUp: resuming
	"If an instance is open in the world, we automatically start its server
	(but only if we're starting up the system)"
	resuming ifFalse: [^ self].
	self findInWorld ifNotNil: [:cp | cp startServer]