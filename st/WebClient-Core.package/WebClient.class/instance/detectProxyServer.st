initialize
detectProxyServer
	"Automatically detect the proxy server to use"

	proxyServer := ProxyHandler proxyServerFor: self server.
