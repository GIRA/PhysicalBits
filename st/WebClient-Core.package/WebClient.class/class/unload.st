class initialization
unload
	"Unregister WebClient as http socket handler when unloaded"

	self unregisterHttpSocketRequestHandler.
