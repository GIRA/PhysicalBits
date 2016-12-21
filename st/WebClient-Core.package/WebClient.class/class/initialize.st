class initialization
initialize
	"WebClient initialize"

	FlagAcceptCookies := 1.
	FlagAllowAuth := 2.
	FlagAllowRedirect := 4.

	"Default proxy handler"
	ProxyHandler := WebUtils.

	self registerHttpSocketRequestHandler.
