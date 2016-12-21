listening
startListener
	"Starts the server's listener process"

	self stopListener.
	listenerProcess := [self runListener] newProcess.
	listenerProcess priority: self listenerProcessPriority.
	listenerProcess name: self class name, '''s listener process'.
	listenerProcess resume.
