tests
test025DuplicateGlobalsAreNotValid.
	self assertInvalid: (program
		globals: {
			varDec name: #a.
			varDec name: #a
		}
		scripts: { 
			proc name: #foo 
				arguments: #() 
				body: (block 
					statements: {})
		}).