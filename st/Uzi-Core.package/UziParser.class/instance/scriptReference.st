grammar-scripts
scriptReference
	^ scriptName token map: [:token |
		(UziScriptRefNode named: token parsedValue) token: token]