tests
test004AssignmentValueShouldBeAnExpression
	"self assertInvalid: 'var a; proc foo() { a = turnOff(D13); }'."
	self assertInvalid: (program
		globals: { varDec name: #a }
		scripts: { 
			proc 
				name: #foo 
				arguments: #() 
				body: (block 
					statements: {
						assignment 
							left: (var named: #a)
							right: (call 
								selector: #turnOff 
								arguments: { nil -> (number value: 13) }
								primitiveName: #turnOff)
					})
		}
		primitives: primitives).

	"self assertInvalid: 'var b; proc foo() { b = if 3 < 4 {3} else {4}}'."
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
								condition: (call
									selector: #<
									arguments: { 
										nil -> (number value: 3).
										nil -> (number value: 4)
									}
									primitiveName: #lessThan)
								true: (block
									statements: { number value: 3 })
								false: (block
									statements: { number value: 4 }))
					})
		}
		primitives: primitives).
	