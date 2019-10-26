tests
test040TickingRateValueShouldAlwaysBePositive
	self assertInvalid: 'task foo() running 0/s {}'.
	self assertInvalid: (program
		globals: #()
		scripts: { 
			task name: #foo 
				arguments: #() 
				state: #running
				tickingRate: (tickingRate value: -1 scale: #s)
				body: (block statements: {})
		}
		primitives: primitives).