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
					statements: {}).
			
			task name: #foo 
				arguments: #() 
				body: (block 
					statements: {})
		}
		primitives: primitives).