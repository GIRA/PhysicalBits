tests
test039TickingRateIsOnlyAllowedIfTaskStateIsSpecified
	self assertInvalid: 'task foo() 1/s {}'.
	self assertInvalid: (program
		globals: #()
		scripts: { 
			task name: #foo 
				arguments: #() 
				state: #once
				tickingRate: (tickingRate value: 1 scale: #s)
				body: (block statements: {})
		}
		primitives: primitives).