httpsocket
registerHttpSocketRequestHandler
	"Register WebClient as an HTTPSocket request handler"

	(HTTPSocket respondsTo: #httpRequestHandler:) ifFalse:[^self].

	"Be polite and only register if no other handler was registered."
	HTTPSocket httpRequestHandler == HTTPSocket
		ifTrue:[HTTPSocket httpRequestHandler: self].
