errors
systemInfoOn: strm
	"Provides system information for a server error report"

	strm nextPutAll: 'VM: ';
		nextPutAll:  WebUtils platformName;
		nextPutAll: ' - ';
		nextPutAll: WebUtils vmVersion;
		cr.

	strm nextPutAll: 'Image: ';
		nextPutAll:  SystemVersion current version asString;
		nextPutAll: ' [';
		nextPutAll: SmalltalkImage current lastUpdateString asString;
		nextPutAll: ']';
		cr.

	strm cr.

	SecurityManager default printStateOn: strm.

	"Additional info available from the windows VM"
	WebUtils platformName = 'Win32' ifTrue:[
		10001 to: 10003 do:[:i| | info |
			info := (SmalltalkImage current getSystemAttribute: i) ifNil:[''].
			info := info copyReplaceAll: String lf with: String cr.
			info isEmpty ifFalse:[strm cr; nextPutAll: info]]].
