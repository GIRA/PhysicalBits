tests
test005ConditionalBranchesShouldBeBlocks
	"self assertInvalid: 'task foo() { if read(D13) turnOff(D13); }'."
	self assertInvalid: (program
		globals: #()
		scripts: { 
			task 
				name: #foo 
				arguments: #() 
				body: (block 
					statements: {
						conditional
							condition: (call
								selector: #read
								arguments: { nil -> (number value: 13) }
								primitiveName: #read)
							true: (call
								selector: #turnOff
								arguments: { nil -> (number value: 13) }
								primitiveName: #turnOff)
					})
		}
		primitives: primitives).
	
	"self assertInvalid: 'task foo() { if read(D13) {} else turnOn(D13); }'."
	self assertInvalid: (program
		globals: #()
		scripts: { 
			task 
				name: #foo 
				arguments: #() 
				body: (block 
					statements: {
						conditional
							condition: (call
								selector: #read
								arguments: { nil -> (number value: 13) })
							false: (call
								selector: #turnOn
								arguments: { nil -> (number value: 13) })
					})
		}
		primitives: primitives).