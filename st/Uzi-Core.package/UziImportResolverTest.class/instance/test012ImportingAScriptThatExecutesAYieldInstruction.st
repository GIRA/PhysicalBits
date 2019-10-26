tests
test012ImportingAScriptThatExecutesAYieldInstruction
	"
	import t10 = ''test10.uzi'';
	task main() running { t10.foo(); }
	"
	| original expected actual |
	original := program 
		imports: {
			import alias: #t10 path: 'test10.uzi' 
		}
		globals: #()
		scripts: {
			task name: #main
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					call selector: 't10.foo'
				})
		}
		primitives: primitives.
	expected := program 
		imports: {
			import alias: #t10 path: 'test10.uzi' isResolved: true
		}
		globals: #()
		scripts: {
			proc name: #'t10.foo'
				arguments: #()
				body: (block statements: {
					yield new
				}).
			task name: #main				
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					call selector: 't10.foo'
				})
		}
		primitives: primitives.
	actual := ir applyOn: original.
	self assert: expected equals: actual