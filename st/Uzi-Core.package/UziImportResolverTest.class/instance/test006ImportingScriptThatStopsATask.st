tests
test006ImportingScriptThatStopsATask
	"
	import t4 = 'test4.uzi';
	task main() running { stopTask(); }
	"
	| original expected actual |
	original := program 
		imports: {
			import alias: #t4 path: 'test4.uzi' 
		}
		globals: #()
		scripts: {
			task name: #main
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					call script: (ref scriptName: 't4.stopTask')
				})
		}.
	expected := program 
		globals: #()
		scripts: {
			proc name: #'t4.stopTask'
				arguments: #()
				body: (block statements: {
					stop scripts: { ref scriptName: 't4.blink' }
				}).
			task name: #'t4.blink'
				arguments: #()
				state: #running
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
					call script: (ref scriptName: 't4.stopTask')
				})
		}.
	actual := ir applyOn: original.
	self assert: expected equals: actual