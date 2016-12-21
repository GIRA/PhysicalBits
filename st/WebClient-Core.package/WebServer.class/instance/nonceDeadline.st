authentication
nonceDeadline
	"Answer the number of seconds a nonce is considered valid for digest auth
	by the server. After this period, the client will have to reauthenticate.
	The default is 30 minutes."

	^30*60 