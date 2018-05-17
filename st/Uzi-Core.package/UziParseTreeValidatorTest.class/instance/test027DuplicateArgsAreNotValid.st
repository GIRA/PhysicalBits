tests
test027DuplicateArgsAreNotValid.
	self assertInvalid: (program
		globals: #()
		scripts: { 
			proc name: #foo 
				arguments: {
					varDec name: #a. 
					varDec name: #a 
				}
				body: (block 
					statements: {})
		}).