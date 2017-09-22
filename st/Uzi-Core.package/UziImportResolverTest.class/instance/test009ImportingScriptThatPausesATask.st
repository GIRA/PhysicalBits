tests
test009ImportingScriptThatPausesATask
	"
	import t7 = 'test7.uzi';
	task main() running { t7.pauseTask(); }
	"
	| original expected actual |
	original := program 
		imports: {
			import alias: #t7 path: 'test7.uzi' 
		}
		globals: #()
		scripts: {
			task name: #main
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					call script: { ref scriptName: 't7.pauseTask' }
				})
		}.
	expected := program 
		globals: #()
		scripts: {
			proc name: #'t7.pauseTask'
				arguments: #()
				body: (block statements: {
					pause scripts: { ref scriptName: 't7.blink' }
				}).
			task name: #'t7.blink'
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
					call script: { ref scriptName: 't7.pauseTask' }
				})
		}.
	actual := ir applyOn: original.
	self assert: expected equals: actual