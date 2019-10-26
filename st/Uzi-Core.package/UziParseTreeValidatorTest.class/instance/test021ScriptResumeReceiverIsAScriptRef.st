tests
test021ScriptResumeReceiverIsAScriptRef
	"self assertInvalid: 'task foo() { resume 5; }'"
	self assertInvalid: (program
		globals: #()
		scripts: { 
			proc 
				name: #foo 
				arguments: #() 
				body: (block 
					statements: { resume scripts: { number value: 5 }})
		}
		primitives: primitives).