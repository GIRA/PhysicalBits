tests
test013ImportingAScriptThatExecutesAForeverLoop
	"
	import t11 = ''test11.uzi'';
	task main() running { t11.foo(); }
	"
	| original expected actual |
	original := program 
		imports: {
			import alias: #t11 path: 'test11.uzi' 
		}
		globals: #()
		scripts: {
			task name: #main
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					call selector: 't11.foo'
				})
		}
		primitives: primitives.
	expected := program 
		imports: {
			import alias: #t11 path: 'test11.uzi' isResolved: true
		}
		globals: #()
		scripts: {
			proc name: #'t11.foo'
				arguments: #()
				body: (block statements: {
					forever body: (block statements: {
						yield new
					})
				}).
			task name: #main				
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					call selector: 't11.foo'
				})
		}
		primitives: primitives.
	actual := linker applyOn: original.
	self assert: expected equals: actual