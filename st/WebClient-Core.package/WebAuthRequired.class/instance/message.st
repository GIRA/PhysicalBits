accessing
message
	"Answer a default message for the user to ask for input"

	self isProxyAuth ifTrue:[
		^'The proxy server at "', client proxyServerName, '" requires authentication.'.
	].

	^'The server at "', client serverName, '" requires authentication.'.