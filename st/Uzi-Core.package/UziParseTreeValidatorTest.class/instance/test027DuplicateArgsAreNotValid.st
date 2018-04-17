tests
test027DuplicateArgsAreNotValid.
	self assertInvalid: (program
		globals: #()
		scripts: { 
			proc name: #foo 
				arguments: {
					var named: #a. 
					var named: #a 
				}
				body: (block 
					locals: #()
					statements: {})
		}).