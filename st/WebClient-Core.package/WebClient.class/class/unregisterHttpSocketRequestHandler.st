httpsocket
unregisterHttpSocketRequestHandler
	"Unregister WebClient as an HTTPSocket request handler"

	(HTTPSocket respondsTo: #httpRequestHandler:) ifFalse:[^self].

	HTTPSocket httpRequestHandler == self
		ifTrue:[HTTPSocket httpRequestHandler: nil].
