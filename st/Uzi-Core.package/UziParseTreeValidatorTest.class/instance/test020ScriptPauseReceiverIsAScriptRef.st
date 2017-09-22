tests
test020ScriptPauseReceiverIsAScriptRef
	"self assertInvalid: 'task foo() { pause 5; }'"
	self assertInvalid: (program
		globals: #()
		scripts: { 
			proc 
				name: #foo 
				arguments: #() 
				body: (block 
					locals: #() 
					statements: { pause scripts: { UziNumberNode value: 5 }})
		}).