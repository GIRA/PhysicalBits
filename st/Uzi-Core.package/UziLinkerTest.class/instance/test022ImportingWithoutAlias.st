tests
test022ImportingWithoutAlias
	"
	import ''test13.uzi'';
	"
	| original expected actual |
	original := program 
		imports: {
			import path: 'test13.uzi'
		}
		globals: #()
		scripts: {}
		primitives: primitives.
	expected := program 
		imports: {
			import path: 'test13.uzi' isResolved: true
		}
		globals: {
			varDec name: #'a' value: (number value: 100).
			varDec name: #'b' value: (number value: 200).
			varDec name: #'c' value: (number value: 300).
		}
		scripts: {
			task name: #'foo'
				arguments: #()
				state: #stopped
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
	actual := linker applyOn: original.
	self assert: expected equals: actual