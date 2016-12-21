proxy
proxyExceptionsDo: aBlock
	"Evaluate aBlock with all registered proxy exceptions. 
	Uses the information from HTTPSocket."

	HTTPSocket httpProxyExceptions do: aBlock.