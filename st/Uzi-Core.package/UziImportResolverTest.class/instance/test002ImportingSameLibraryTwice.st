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
					call selector: #write
						arguments: {
							nil -> (call selector: #'test2.foo').
							nil -> (call selector: #'test1.foo')
						}
						primitiveName: #write
				})
		}
		primitives: primitives.
	expected := program 
		imports: {
			import alias: #test1 path: 'test.uzi' isResolved: true.
			import alias: #test2 path: 'test.uzi' isResolved: true
		}
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
					call selector: #write
						arguments: {
							nil -> (call selector: #'test2.foo').
							nil -> (call selector: #'test1.foo')
						}
						primitiveName: #write
				})
		}
		primitives: primitives.
	actual := ir applyOn: original.
	self assert: expected equals: actual