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
					call selector: 't7.pauseTask'
				})
		}
		primitives: primitives.
	expected := program 
		imports: {
			import alias: #t7 path: 'test7.uzi' isResolved: true
		}
		globals: #()
		scripts: {
			proc name: #'t7.pauseTask'
				arguments: #()
				body: (block statements: {
					pause scripts: { 't7.blink' }
				}).
			task name: #'t7.blink'
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					call selector: #'t7.toggle'
						arguments: { nil -> (pin type: #D number: 13) }
						primitiveName: #toggle
				}).
			task name: #main				
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					call selector: 't7.pauseTask'
				})
		}
		primitives: primitives.
	actual := ir applyOn: original.
	self assert: expected equals: actual