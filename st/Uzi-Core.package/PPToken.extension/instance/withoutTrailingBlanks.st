*Uzi-Core-Parser
withoutTrailingBlanks
	^ PPToken 
		on: collection
		start: start
		stop: start + self inputValue withoutTrailingBlanks size - 1
		value: value
		