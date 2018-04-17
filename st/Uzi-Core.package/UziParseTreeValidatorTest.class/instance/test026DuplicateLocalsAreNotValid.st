tests
test026DuplicateLocalsAreNotValid.
	self assertInvalid: (program
		globals: #()
		scripts: { 
			proc name: #foo 
				arguments: #() 
				body: (block 
					locals: {
						varDec name: #a. 
						varDec name: #a 
					}
					statements: {})
		}).