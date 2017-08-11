tests
testConditionalBranchesShouldBeBlocks
	"self assertInvalid: 'task foo() { if read(D13) turnOff(D13); }'."
	self assertInvalid: (program
		variableDeclarations: #()
		scripts: { 
			task 
				name: #foo 
				arguments: #() 
				body: (block 
					statements: {
						conditional
							condition: (prim
								selector: #read
								arguments: { number value: 13 })
							true: (prim
								selector: #turnOff
								arguments: { number value: 13 })
					})
		}).
	
	"self assertInvalid: 'task foo() { if read(D13) {} else turnOn(D13); }'."
	self assertInvalid: (program
		variableDeclarations: #()
		scripts: { 
			task 
				name: #foo 
				arguments: #() 
				body: (block 
					statements: {
						conditional
							condition: (prim
								selector: #read
								arguments: { number value: 13 })
							false: (prim
								selector: #turnOn
								arguments: { number value: 13 })
					})
		}).