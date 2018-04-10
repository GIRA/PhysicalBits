tests
test002ImportingSameLibraryTwice
	"
	import test1 = 'test.uzi';
	import test2 = 'test.uzi';
	task main() { write(test2.foo(), test1.foo()); }
	"
	| original expected actual |
	original := program 
		imports: {
			import alias: #test1 path: 'test.uzi'.
			import alias: #test2 path: 'test.uzi' 			
		}
		globals: #()
		scripts: {
			task name: #main
				arguments: #()
				body: (block statements: {
					prim selector: #write
						arguments: {
							call script: (ref scriptName: #'test2.foo').
							call script: (ref scriptName: #'test1.foo')
						}
				})
		}.
	expected := program 
		globals: { 
			varDec name: #'test1.v' value: (number value: 42).
			varDec name: #'test2.v' value: (number value: 42).
		}
		scripts: {
			func name: #'test1.foo'
				arguments: #()
				body: (block statements: {
					return value: (number value: 42)
				}).
			func name: #'test2.foo'
				arguments: #()
				body: (block statements: {
					return value: (number value: 42)
				}).
			task name: #main
				arguments: #()
				body: (block statements: {
					prim selector: #write
						arguments: {
							call script: (ref scriptName: #'test2.foo').
							call script: (ref scriptName: #'test1.foo')
						}
				})
		}.
	actual := ir applyOn: original.
	self assert: expected equals: actual