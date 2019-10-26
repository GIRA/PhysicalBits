tests
test030ForCounterShouldBeAVariable
	"self assertInvalid: 'task foo() running { 
		for 1 = 1 to 10 { toggle(D13); }
	}'."
	self assertInvalid: (program
		globals: #()
		scripts: { 
			task
				name: #foo 
				arguments: #() 
				state: #running
				tickingRate: nil
				body: (block 
					statements: {
						for 
							counter: (number value: 1)
							start: (number value: 1)
							stop: (number value: 10)
							body: (block statements: {
								(call selector: #turnOn
									arguments: { nil -> (number value: 13) }
									primitiveName: #turnOn)
							})
					})
		}
		primitives: primitives).