tests
test007ImportingScriptThatStartsATask
	"
	import t5 = 'test5.uzi';
	task main() running { startTask(); }
	"
	| original expected actual |
	original := program 
		imports: {
			import alias: #t5 path: 'test5.uzi' 
		}
		globals: #()
		scripts: {
			task name: #main
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					call script: (ref scriptName: 't5.startTask')
				})
		}.
	expected := program 
		globals: #()
		scripts: {
			proc name: #'t5.startTask'
				arguments: #()
				body: (block statements: {
					start scripts: { ref scriptName: 't5.blink' }
				}).
			task name: #'t5.blink'
				arguments: #()
				state: #stopped
				tickingRate: nil
				body: (block statements: {
					prim selector: #toggle
						arguments: { number value: 13 }
				}).
			task name: #main				
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					call script: (ref scriptName: 't5.startTask')
				})
		}.
	actual := ir applyOn: original.
	self assert: expected equals: actual