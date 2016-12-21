initialize
initializeFromUrl: urlString
	"Initialize the client from a http url or string"

	| urlStream |
	urlStream := urlString readStream.
	"Skip past scheme if present"
	urlStream upToAll: '://'.
	urlStream atEnd 
		ifTrue:[rawUrl := urlString]
		ifFalse:[rawUrl := urlStream upTo: $/; upToEnd].
	(rawUrl beginsWith: '/') ifFalse:[rawUrl := '/', rawUrl].
