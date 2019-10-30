tests
test016SpecifyingAnInitBlockToStartAnImportedTask
	"
	import t13 = ''test13.uzi'' {
		start foo;
	}
	"
	| original expected actual |
	original := program 
		imports: {
			import alias: #t13 path: 'test13.uzi' initializationBlock: (block statements: {
				start scripts: { 'foo' }
			})
		}
		globals: #()
		scripts: {}
		primitives: primitives.
	expected := program 
		imports: {
			import alias: #t13 path: 'test13.uzi' initializationBlock: (block statements: {
				start scripts: { 'foo' }
			}) isResolved: true
		}
		globals: {
			varDec name: #'t13.a' value: (number value: 100).
			varDec name: #'t13.b' value: (number value: 200).
			varDec name: #'t13.c' value: (number value: 300).
		}
		scripts: {
			task name: #'t13.foo'
				arguments: #()
				state: #running
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