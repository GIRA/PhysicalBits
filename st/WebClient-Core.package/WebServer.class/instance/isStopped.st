testing
isStopped
	"Is the server still running?"

	^listenerProcess == nil or:[listenerProcess isTerminated]