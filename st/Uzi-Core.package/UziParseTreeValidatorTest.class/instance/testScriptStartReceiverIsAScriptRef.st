tests
testScriptStartReceiverIsAScriptRef
	"self assertInvalid: 'task foo() { start 5; }'"
	self assertInvalid: (program
		variableDeclarations: #()
		scripts: { 
			proc 
				name: #foo 
				arguments: #() 
				body: (block 
					locals: #() 
					statements: { start script: (UziNumberNode value: 5)})
		}).