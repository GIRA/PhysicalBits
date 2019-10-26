tests
test003ConditionalsAreNotExpressions
	"self assertInvalid: 'task foo() { write(D13, if 3 > 4 { turnOff(D13); }); }'."
	self assertInvalid: (program
		globals: #()
		scripts: { 
			task
				name: #foo 
				arguments: #() 
				body: (block 
					statements: {
						call 
							selector: #write
							arguments: {
								nil -> (number value: 13).
								nil -> (conditional
									condition: (call
										selector: #greaterThan
										arguments: { 
											nil -> (number value: 3).
											nil -> (number value: 4)
										}
										primitiveName: #greaterThan)
									true: (block
										statements: { call
											selector: #turnOff
											arguments: { nil -> (number value: 13) } 
											primitiveName: #turnOff
										}))
							}
							primitiveName: #write
					})
		}
		primitives: primitives)
	