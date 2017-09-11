grammar-script references
scriptReference
	^ (ws , identifier , ws) token map: [:token | 
		(UziScriptRefNode scriptName: token parsedValue second) token: token]