tests
test024DuplicateScriptNamesAreNotValid
	self assertInvalid: '
	func test1 () {}
	proc test1() {}'.
	self assertInvalid: (program
		globals: #()
		scripts: { 
			proc name: #foo 
				arguments: #() 
				body: (block 
					locals: #() 
					statements: {}).
			
			task name: #foo 
				arguments: #() 
				body: (block 
					locals: #() 
					statements: {})
		}).