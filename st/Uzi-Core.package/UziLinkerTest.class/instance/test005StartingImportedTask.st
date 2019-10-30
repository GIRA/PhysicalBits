tests
test005StartingImportedTask
	"
	import t3 = 'test3.uzi';
	task main() running { start t3.blink13; }
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
					start scripts: { 't3.blink13' }
				})
		}
		primitives: primitives.
	expected := program 
		imports: {
			import alias: #t3 path: 'test3.uzi' isResolved: true
		}
		globals: #()
		scripts: {
			task name: #'t3.blink13'
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					call selector: #'t3.toggle'
						arguments: { nil -> (pin type: #D number: 13) }
						primitiveName: #toggle
				}).
			task name: #main				
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					start scripts: { 't3.blink13' }
				})
		}
		primitives: primitives.
	actual := linker applyOn: original.
	self assert: expected equals: actual