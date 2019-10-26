tests
test009ScriptStartReceiverIsAScriptRef
	"self assertInvalid: 'task foo() { start 5; }'"
	self assertInvalid: (program
		globals: #()
		scripts: { 
			proc 
				name: #foo 
				arguments: #() 
				body: (block 
					statements: { start scripts: { number value: 5 }})
		}
		primitives: primitives).