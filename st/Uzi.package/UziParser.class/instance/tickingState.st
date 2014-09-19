scripts
tickingState
	^ super tickingState map: [:token | token inputValue asSymbol]