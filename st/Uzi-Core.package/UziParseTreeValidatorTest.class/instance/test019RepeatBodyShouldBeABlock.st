tests
test019RepeatBodyShouldBeABlock
	"self assertInvalid: 'task foo() running { 
		repeat 5 toggle(D13);
	}'."
	self assertInvalid: (program
		globals: #()
		scripts: { 
			task
				name: #foo 
				arguments: #() 
				state: #running
				tickingRate: nil
				body: (block 
					statements: {
						repeat
							times: (number value: 5)
							body: (prim
								selector: #turnOn
								arguments: { number value: 13 })
					})
		}).