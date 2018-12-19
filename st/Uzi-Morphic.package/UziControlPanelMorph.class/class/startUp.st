system startup
startUp
	"If an instance is open in the world, we automatically start its server"
	self findInWorld ifNotNil: [:cp | cp startServer]