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
					locals: #() 
					statements: { start scripts: { UziNumberNode value: 5 }})
		}).