grammar-scripts
scriptReference
	^ (identifier optional , scriptName) token 
		map: [:token || alias name |
			alias := token parsedValue first.
			name := token parsedValue second.
			alias ifNotNil: [name := (alias , '@' , name) asSymbol].
			(UziScriptRefNode named: name) token: token]