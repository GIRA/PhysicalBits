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
					call selector: 't4.stopTask'
				})
		}
		primitives: primitives.
	expected := program 
		imports: {
			import alias: #t4 path: 'test4.uzi' isResolved: true
		}
		globals: #()
		scripts: {
			proc name: #'t4.stopTask'
				arguments: #()
				body: (block statements: {
					stop scripts: { 't4.blink' }
				}).
			task name: #'t4.blink'
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					call selector: #'t4.toggle'
						arguments: { nil -> (pin type: #D number: 13) }
						primitiveName: #toggle
				}).
			task name: #main				
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					call selector: 't4.stopTask'
				})
		}
		primitives: primitives.
	actual := linker applyOn: original.
	self assert: expected equals: actual