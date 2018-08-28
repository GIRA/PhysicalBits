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
					call selector: 't5.startTask'
				})
		}.
	expected := program 
		globals: #()
		scripts: {
			proc name: #'t5.startTask'
				arguments: #()
				body: (block statements: {
					start scripts: { 't5.blink' }
				}).
			task name: #'t5.blink'
				arguments: #()
				state: #stopped
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
					call selector: 't5.startTask'
				})
		}.
	actual := ir applyOn: original.
	self assert: expected equals: actual