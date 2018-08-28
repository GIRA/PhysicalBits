tests
test014ImportingAScriptThatExecutesAForLoop
	"
	import t12 = ''test12.uzi'';
	task main() running { t12.foo(); }
	"
	| original expected actual |
	original := program 
		imports: {
			import alias: #t12 path: 'test12.uzi' 
		}
		globals: #()
		scripts: {
			task name: #main
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					call selector: 't12.foo'
				})
		}.
	expected := program 
		globals: #()
		scripts: {
			proc name: #'t12.foo'
				arguments: #()
				body: (block statements: {
					for counter: (varDec name: #i)
						start: (number value: 1)
						stop: (number value: 10)
						step: (number value: 1)
						body: (block statements: {
							yield new
						})
				}).
			task name: #main				
				arguments: #()
				state: #running
				tickingRate: nil
				body: (block statements: {
					call selector: 't12.foo'
				})
		}.
	actual := ir applyOn: original.
	self assert: expected equals: actual