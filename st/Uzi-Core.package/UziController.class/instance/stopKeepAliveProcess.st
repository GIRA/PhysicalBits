keep alive
stopKeepAliveProcess
	"In order to correctly terminate the process and set its variable
	to nil, we need to store it in a temp variable, so that we set the 
	variable to nil before terminating the process. Since we're in the
	same process if we terminate it before this won't get executed"
	| p |
	keepAlive isNil
		ifTrue: [^ self].
	p := keepAlive.
	keepAlive := nil.
	p terminate