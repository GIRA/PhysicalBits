tests
test017PrimitiveCallArgumentsShouldBeExpressions
	self assertInvalid: 'var a; task foo() stopped { turnOn(a = 5); }'.
	self assertInvalid: 'task foo() stopped { turnOn(turnOn(D13)); }'.
	
	"self assertInvalid: 'task foo() { turnOn({turnOn(D13);});}'."
	self assertInvalid: (program
		globals: #()
		scripts: { 
			proc 
				name: #foo 
				arguments: #() 
				body: (block 
					statements: {
						call
							selector: #turnOn
							arguments: {
								block statements: {
									call selector: #turnOn
										arguments: { nil -> (number value: 13) }
										primitiveName: #turnOn
								}
							}
							primitiveName: #turnOn
					})
		}).