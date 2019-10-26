tests
test031ForStartShouldBeAnExpression
	"self assertInvalid: 'task foo() running { 
		for a = toggle(D13) to 10 { toggle(D13); }
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
							start: (call
								selector: #toggle
								arguments: { nil -> (number value: 13) }
								primitiveName: #toggle)
							stop: (number value: 10)
							body: (block statements: {
								(call
									selector: #turnOn
									arguments: { nil -> (number value: 13) }
									primitiveName: #turnOn)
							})
					})
		}
		primitives: primitives).