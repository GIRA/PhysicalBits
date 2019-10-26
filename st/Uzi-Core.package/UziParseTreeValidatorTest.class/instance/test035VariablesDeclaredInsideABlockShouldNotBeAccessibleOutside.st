tests
test035VariablesDeclaredInsideABlockShouldNotBeAccessibleOutside
	"self assertInvalid: 'task foo() running { 
		if 3 > 4 { var a = 5; }
		a = a + 1;
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
						conditional
							condition: (call
								selector: #greaterThan
								arguments: { 
									nil -> (number value: 3).
									nil -> (number value: 4)
								}
								primitiveName: #greaterThan)
							true: (block
								statements: { 
									varDec 
										name: #a 
										value: (number value: 5).
								}).
						assignment 
							left: (var named: #a)
							right: (call
								selector: #+ 
								arguments: {
									nil -> (var named: #a).
									nil -> (number value: 1)
								}
								primitiveName: #add).
					})
		}
		primitives: primitives).