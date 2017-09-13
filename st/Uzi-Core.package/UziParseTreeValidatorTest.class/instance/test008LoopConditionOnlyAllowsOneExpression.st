tests
test008LoopConditionOnlyAllowsOneExpression
	"self assertInvalid: 'task foo() { until (1; read(D13)); }'"
	self assertInvalid: (program
		globals: #()
		scripts: { 
			task
				name: #foo 
				arguments: #() 
				body: (block 
					statements: {
						until
							condition: (block
								statements: {
									number value: 1.
									prim selector: #read 
										arguments: { number value: 13}
								})
					})
		}).