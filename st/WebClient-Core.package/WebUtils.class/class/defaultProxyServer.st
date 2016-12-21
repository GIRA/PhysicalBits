proxy
defaultProxyServer
	"Answer the default proxy server to use."

	HTTPSocket httpProxyServer isEmptyOrNil ifTrue:[^''].

	"Note: This is a hack since HTTPSocket is too specific for our use here."
	HTTPSocket httpProxyPort = 80 ifTrue:[
		"Assumes that port 80 means 'use default'"
		^HTTPSocket httpProxyServer ifNil:['']
	] ifFalse:[
		^HTTPSocket httpProxyServer, ':', HTTPSocket httpProxyPort
	].