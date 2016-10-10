scripts
scriptName
	^ super scriptName map: [:token |
		"Remove the first character ($#)"
		token inputValue allButFirst asSymbol]