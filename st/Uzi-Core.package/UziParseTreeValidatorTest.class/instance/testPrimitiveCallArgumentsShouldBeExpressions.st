tests
testPrimitiveCallArgumentsShouldBeExpressions
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
						prim
							selector: #turnOn
							arguments: {
								block statements: {
									prim selector: #turnOn
										arguments: { number value: 13 }
								}
							}
					})
		}).