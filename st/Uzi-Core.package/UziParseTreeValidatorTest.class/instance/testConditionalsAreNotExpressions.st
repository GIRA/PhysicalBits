tests
testConditionalsAreNotExpressions
	"self assertInvalid: 'task foo() { write(D13, if 3 > 4 { turnOff(D13); }); }'."
	self assertInvalid: (program
		globals: #()
		scripts: { 
			task
				name: #foo 
				arguments: #() 
				body: (block 
					statements: {
						prim 
							selector: #write
							arguments: {
								number value: 13.								
								conditional
									condition: (prim
										selector: #greaterThan
										arguments: { 
											number value: 3.
											number value: 4 
										})
									true: (block
										statements: { prim
											selector: #turnOff
											arguments: { number value: 13 } 
										})
							}
					})
		}).
	