tests
test031ForStartShouldBeAnExpression
	"self assertInvalid: 'task foo() running { 
		for a = toggle(D13) to 10 { toggle(D13); }
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
							start: (prim
								selector: #toggle
								arguments: { number value: 13 })
							stop: (number value: 10)
							body: (block statements: {
								(prim
									selector: #turnOn
									arguments: { number value: 13 })
							})
					})
		}).