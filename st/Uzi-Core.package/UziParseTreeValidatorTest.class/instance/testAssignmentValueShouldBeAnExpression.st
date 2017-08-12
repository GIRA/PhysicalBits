tests
testAssignmentValueShouldBeAnExpression
	"self assertInvalid: 'var a; proc foo() { a = turnOff(D13); }'."
	self assertInvalid: (program
		globals: { varDec name: #a }
		scripts: { 
			proc 
				name: #foo 
				arguments: #() 
				body: (block 
					locals: #() 
					statements: {
						assignment 
							left: (var named: #a)
							right: (prim 
								selector: #turnOff 
								arguments: { number value: 13 })
					})
		}).

	"self assertInvalid: 'var b; proc foo() { b = if 3 > 4 {3} else {4}}'."
	self assertInvalid: (program
		globals: { varDec name: #b }
		scripts: { 
			proc 
				name: #foo 
				arguments: #() 
				body: (block 
					statements: {
						assignment 
							left: (var named: #b)
							right: (conditional
								condition: (prim
									selector: #lessThan
									arguments: { 
										number value: 3.
										number value: 4 
									})
								true: (block
									statements: { number value: 3 })
								false: (block
									statements: { number value: 4 }))
					})
		}).
	