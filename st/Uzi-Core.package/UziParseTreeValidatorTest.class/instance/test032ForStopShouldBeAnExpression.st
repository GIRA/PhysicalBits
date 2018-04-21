tests
test032ForStopShouldBeAnExpression
	"self assertInvalid: 'task foo() running { 
		for a = 1 to toggle(D13) { toggle(D13); }
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
						for 
							counter: (var named: #a)
							start: (number value: 1)
							stop: (prim
								selector: #toggle
								arguments: { number value: 13 })
							body: (block statements: {
								(prim
									selector: #turnOn
									arguments: { number value: 13 })
							})
					})
		}).