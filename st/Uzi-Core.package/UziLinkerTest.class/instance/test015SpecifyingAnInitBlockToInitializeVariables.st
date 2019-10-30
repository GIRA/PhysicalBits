tests
test015SpecifyingAnInitBlockToInitializeVariables
	"
	import t13 = ''test13.uzi'' {
		a = 10;
		b = 20;
		c = 30;
	}
	"
	| original expected actual |
	original := program 
		imports: {
			import alias: #t13 path: 'test13.uzi' initializationBlock: (block statements: {
				assignment left: (var named: #a) right: (number value: 10).
				assignment left: (var named: #b) right: (number value: 20).
				assignment left: (var named: #c) right: (number value: 30).
			})
		}
		globals: #()
		scripts: {}
		primitives: primitives.
	expected := program 
		imports: {
			import alias: #t13 path: 'test13.uzi' initializationBlock: (block statements: {
				assignment left: (var named: #a) right: (number value: 10).
				assignment left: (var named: #b) right: (number value: 20).
				assignment left: (var named: #c) right: (number value: 30).
			}) isResolved: true
		}
		globals: {
			varDec name: #'t13.a' value: (number value: 10).
			varDec name: #'t13.b' value: (number value: 20).
			varDec name: #'t13.c' value: (number value: 30).
		}
		scripts: {
			task name: #'t13.foo'
				arguments: #()
				state: #stopped
				tickingRate: (tickingRate value: 1 scale: #s)
				body: (block statements: {
					call selector: #'t13.toggle'
						arguments: {
							nil -> (pin type: #D number: 12) .
						}
						primitiveName: #toggle
				}).
			task name: #'t13.setup'				
				arguments: #()
				state: #once
				tickingRate: nil
				body: (block statements: {
					assignment left: (var named: #'t13.a') right: (number value: 1).
					assignment left: (var named: #'t13.b') right: (number value: 2).
					assignment left: (var named: #'t13.c') right: (number value: 3).
				})
		}
		primitives: primitives.
	actual := linker applyOn: original.
	self assert: expected equals: actual