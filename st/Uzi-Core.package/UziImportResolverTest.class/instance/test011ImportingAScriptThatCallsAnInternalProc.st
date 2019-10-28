tests
test011ImportingAScriptThatCallsAnInternalProc
	"
	import t9 = ''test9.uzi'';
	task main() running { t9.blink(); }
	"
	| original expected actual |
	original := program 
		imports: {
			import alias: #t9 path: 'test9.uzi' 
		}
		globals: #()
		scripts: {
			task name: #main
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					call selector: 't9.blink'
				})
		}
		primitives: primitives.
	expected := program 
		imports: {
			import alias: #t9 path: 'test9.uzi' isResolved: true
		}
		globals: #()
		scripts: {
			proc name: #'t9.toggleAndWait'
				arguments: {
					varDec name: 'pin'.
					varDec name: 'ms'.
				}
				body: (block statements: {
					call selector: #'t9.toggle'
						arguments: { nil -> (var named: 'pin') }
						primitiveName: #toggle.	
					call selector: #'t9.delayMs'
						arguments: { nil -> (var named: 'ms') }
						primitiveName: #delayMs
				}).
			proc name: #'t9.blink'
				arguments: #()
				body: (block statements: {
					call selector: 't9.toggleAndWait'
						arguments: {
							nil -> (pin type: #D number: 13).
							nil -> (number value: 100)
						}
				}).
			task name: #main				
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					call selector: 't9.blink'
				})
		}
		primitives: primitives.
	actual := ir applyOn: original.
	self assert: expected equals: actual