tests
test034AVariableShouldBeDeclaredBeforeItsFirstUse
	"self assertInvalid: 'task foo() running { 
		a = a + 1;
		var a = 5;
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
						assignment 
							left: (var named: #a)
							right: (prim
								selector: #+ 
								arguments: {
									var named: #a.
									number value: 1
								}
								primitive: (UziPrimitive named: #add)).
						varDec 
							name: #a 
							value: (number value: 5)
					})
		}).