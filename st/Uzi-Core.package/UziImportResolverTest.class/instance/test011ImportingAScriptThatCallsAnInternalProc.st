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
					call script: (ref scriptName: 't9.blink')
				})
		}.
	expected := program 
		globals: #()
		scripts: {
			proc name: #'t9.toggleAndWait'
				arguments: {
					var named: 'pin'.
					var named: 'ms'.
				}
				body: (block statements: {
					prim selector: #toggle
						arguments: { var named: 'pin' }.					
					prim selector: #delayMs
						arguments: { var named: 'ms' }
						primitive: (UziPrimitive named: #yieldTime)
				}).
			proc name: #'t9.blink'
				arguments: #()
				body: (block statements: {
					call script: (ref scriptName: 't9.toggleAndWait')
						arguments: {
							nil -> (number value: 13).
							nil -> (number value: 100)
						}
				}).
			task name: #main				
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					call script: (ref scriptName: 't9.blink')
				})
		}.
	actual := ir applyOn: original.
	self assert: expected equals: actual