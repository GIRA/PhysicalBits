tests
test036VariablesDeclaredInsideABlockShouldNotCollideWithVariablesAlreadyDeclaredOutside
	"self assertInvalid: 'task foo() running { 
		var a = 5;
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
						varDec 
							name: #a 
							value: (number value: 5).
						conditional
							condition: (prim
								selector: #greaterThan
								arguments: { 
									number value: 3.
									number value: 4 
								})
							true: (block
								statements: { 
									varDec 
										name: #a 
										value: (number value: 5).
								}).
						assignment 
							left: (var named: #a)
							right: (prim
								selector: #+ 
								arguments: {
									var named: #a.
									number value: 1
								}
								primitiveName: #add).
					})
		}).