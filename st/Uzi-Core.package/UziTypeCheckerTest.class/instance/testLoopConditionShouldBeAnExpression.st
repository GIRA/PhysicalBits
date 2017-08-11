tests
testLoopConditionShouldBeAnExpression
	self assertInvalid: 'task foo() { while turnOn(D13) { turnOff(D13); }}'.
	self assertInvalid: 'var a; task foo() { while a = 3 { turnOff(D13); }}'.
	
	"self assertInvalid: 'var a; task foo() { do { turnOff(D13); } while {a}; }'"
	self assertInvalid: (program
		variableDeclarations: { varDec name: #a }
		scripts: { 
			task
				name: #foo 
				arguments: #() 
				body: (block 
					locals: #() 
					statements: {
						doUntil
							condition: (block statements: { var named: #a })
							body: (block statements: {
								prim selector: #turnOff
									arguments: { number value: 13 }
							})
					})
		}).