tests
test008ImportingScriptThatContainsRepeatLoop
	"
	import t6 = 'test6.uzi';
	task main() running { t6.foo(); }
	"
	| original expected actual |
	original := program 
		imports: {
			import alias: #t6 path: 'test6.uzi' 
		}
		globals: #()
		scripts: {
			task name: #main
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					call script: (ref scriptName: 't6.foo')
				})
		}.
	expected := program 
		globals: #()
		scripts: {
			proc name: #'t6.foo'
				arguments: #()
				body: (block statements: {
					repeat
						times: (number value: 5)
						body: (block
							statements: {
								prim
									selector: #toggle
									arguments: { pin type: #D number: 13 }
							})
				}).
			task name: #main				
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					call script: (ref scriptName: 't6.foo')
				})
		}.
	actual := ir applyOn: original.
	self assert: expected equals: actual