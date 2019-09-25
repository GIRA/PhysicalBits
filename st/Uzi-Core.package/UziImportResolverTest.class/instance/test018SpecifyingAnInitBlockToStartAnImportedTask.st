tests
test018SpecifyingAnInitBlockToStartAnImportedTask
	"
	import t13 = ''test13.uzi'' {
		resume foo;
	}
	"
	| original expected actual |
	original := program 
		imports: {
			import alias: #t13 path: 'test13.uzi' initializationBlock: (block statements: {
				resume scripts: { 'foo' }
			})
		}
		globals: #()
		scripts: {}.
	expected := program 
		imports: {
			import alias: #t13 path: 'test13.uzi' initializationBlock: (block statements: {
				resume scripts: { 'foo' }
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
					call selector: #toggle
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
		}.
	actual := ir applyOn: original.
	self assert: expected equals: actual