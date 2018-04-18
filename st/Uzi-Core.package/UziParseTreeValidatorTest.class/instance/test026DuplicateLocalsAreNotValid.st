tests
test026DuplicateLocalsAreNotValid.
	self assertInvalid: (program
		globals: #()
		scripts: { 
			proc name: #foo 
				arguments: #() 
				body: (block 
					statements: {
						varDec name: #a. 
						varDec name: #a 
					})
		}).