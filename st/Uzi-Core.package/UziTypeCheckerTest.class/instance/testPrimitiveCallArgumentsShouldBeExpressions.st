tests
testPrimitiveCallArgumentsShouldBeExpressions
	self assertInvalid: 'var a; task foo() { turnOn(a = 5); }'.
	self assertInvalid: 'task foo() { turnOn(turnOn(D13)); }'.
	
	"self assertInvalid: 'task foo() { turnOn({turnOn(D13);});}'."
	self assertInvalid: (program
		variableDeclarations: #()
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