tests
test032ForStopShouldBeAnExpression
	"self assertInvalid: 'task foo() running { 
		for a = 1 to toggle(D13) { toggle(D13); }
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
							counter: (varDec name: #a)
							start: (number value: 1)
							stop: (call
								selector: #toggle
								arguments: { nil -> (number value: 13) }
								primitiveName: #toggle)
							body: (block statements: {
								(call
									selector: #turnOn
									arguments: { nil -> (number value: 13) }
									primitiveName: #turnOn)
							})
					})
		}
		primitives: primitives).