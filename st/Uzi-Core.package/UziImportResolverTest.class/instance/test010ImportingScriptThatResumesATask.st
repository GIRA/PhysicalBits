tests
test010ImportingScriptThatResumesATask
	"
	import t8 = 'test8.uzi';
	task main() running { t8.resumeTask(); }
	"
	| original expected actual |
	original := program 
		imports: {
			import alias: #t8 path: 'test8.uzi' 
		}
		globals: #()
		scripts: {
			task name: #main
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					call script: (ref scriptName: 't8.resumeTask')
				})
		}.
	expected := program 
		globals: #()
		scripts: {
			proc name: #'t8.resumeTask'
				arguments: #()
				body: (block statements: {
					resume scripts: { ref scriptName: 't8.blink' }
				}).
			task name: #'t8.blink'
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
					call script: (ref scriptName: 't8.resumeTask')
				})
		}.
	actual := ir applyOn: original.
	self assert: expected equals: actual