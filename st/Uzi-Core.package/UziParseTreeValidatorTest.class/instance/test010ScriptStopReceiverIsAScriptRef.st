tests
test010ScriptStopReceiverIsAScriptRef
	"self assertInvalid: 'task foo() { stop 5; }'"
	self assertInvalid: (program
		globals: #()
		scripts: { 
			proc 
				name: #foo 
				arguments: #() 
				body: (block 
					locals: #() 
					statements: { stop scripts: { UziNumberNode value: 5 }})
		}).