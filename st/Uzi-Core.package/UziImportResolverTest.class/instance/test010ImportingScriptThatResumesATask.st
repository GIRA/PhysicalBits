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
					call selector: 't8.resumeTask'
				})
		}.
	expected := program 
		imports: {
			import alias: #t8 path: 'test8.uzi' isResolved: true
		}
		globals: #()
		scripts: {
			proc name: #'t8.resumeTask'
				arguments: #()
				body: (block statements: {
					resume scripts: { 't8.blink' }
				}).
			task name: #'t8.blink'
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					call selector: #toggle
						arguments: { nil -> (pin type: #D number: 13) }
						primitiveName: #toggle
				}).
			task name: #main				
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					call selector: 't8.resumeTask'
				})
		}.
	actual := ir applyOn: original.
	self assert: expected equals: actual