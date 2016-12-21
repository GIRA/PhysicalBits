initialize
flushAuthState
	"Flush all the auth state"

	self flushAuthState: authParams.
	self flushAuthState: proxyParams.