tests
test023ImportingWithoutAliasButWithInitializationBlock
	"
	import ''test13.uzi'' {
		a = 10;
		b = 20;
		c = 30;
		start foo;
	}
	"
	| original expected actual |
	original := program 
		imports: {
			import path: 'test13.uzi' initializationBlock: (block statements: {
				assignment left: (var named: #a) right: (number value: 10).
				assignment left: (var named: #b) right: (number value: 20).
				assignment left: (var named: #c) right: (number value: 30).
				start scripts: { 'foo' }
			})
		}
		globals: #()
		scripts: {}
		primitives: primitives.
	expected := program 
		imports: {
			import path: 'test13.uzi' initializationBlock: (block statements: {
				assignment left: (var named: #a) right: (number value: 10).
				assignment left: (var named: #b) right: (number value: 20).
				assignment left: (var named: #c) right: (number value: 30).
				start scripts: { 'foo' }
			}) isResolved: true
		}
		globals: {
			varDec name: #'a' value: (number value: 10).
			varDec name: #'b' value: (number value: 20).
			varDec name: #'c' value: (number value: 30).
		}
		scripts: {
			task name: #'foo'
				arguments: #()
				state: #running
				tickingRate: (tickingRate value: 1 scale: #s)
				body: (block statements: {
					call selector: #toggle
						arguments: {
							nil -> (pin type: #D number: 12) .
						}
						primitiveName: #toggle
				}).
			task name: #'setup'				
				arguments: #()
				state: #once
				tickingRate: nil
				body: (block statements: {
					assignment left: (var named: #'a') right: (number value: 1).
					assignment left: (var named: #'b') right: (number value: 2).
					assignment left: (var named: #'c') right: (number value: 3).
				})
		}
		primitives: primitives.
	actual := ir applyOn: original.
	self assert: expected equals: actual