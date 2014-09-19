scripts
scriptName
	^ super scriptName map: [:ign :token | token inputValue asSymbol]