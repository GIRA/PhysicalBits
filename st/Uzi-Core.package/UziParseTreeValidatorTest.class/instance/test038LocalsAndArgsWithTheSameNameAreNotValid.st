tests
test038LocalsAndArgsWithTheSameNameAreNotValid.
	self assertInvalid: (program
		globals: #()
		scripts: { 
			proc name: #foo 
				arguments: {
					varDec name: #a. 
				}
				body: (block 
					statements: {
						varDec name: #a 
					})
		}
		primitives: primitives).