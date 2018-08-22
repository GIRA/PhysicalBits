tests
test004StoppingImportedTask
	"
	import t3 = 'test3.uzi';
	task main() running { stop t3.blink13; }
	"
	| original expected actual |
	original := program 
		imports: {
			import alias: #t3 path: 'test3.uzi' 
		}
		globals: #()
		scripts: {
			task name: #main
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					stop scripts: { ref scriptName: 't3.blink13' }
				})
		}.
	expected := program 
		globals: #()
		scripts: {
			task name: #'t3.blink13'
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					prim selector: #toggle
						arguments: { pin type: #D number: 13 }
				}).
			task name: #main				
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					stop scripts: { ref scriptName: 't3.blink13' }
				})
		}.
	actual := ir applyOn: original.
	self assert: expected equals: actual