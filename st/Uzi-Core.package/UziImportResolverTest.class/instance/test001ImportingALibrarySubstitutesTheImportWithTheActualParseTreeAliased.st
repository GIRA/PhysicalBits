tests
test001ImportingALibrarySubstitutesTheImportWithTheActualParseTreeAliased
	"
	import test = 'test.uzi';
	task main() { write(D13, test.foo()); }
	"
	| original expected actual |
	original := program 
		imports: {
			import alias: #test path: 'test.uzi' 
		}
		globals: #()
		scripts: {
			task name: #main
				arguments: #()
				body: (block statements: {
					call selector: #write
						arguments: {
							nil -> (number value: 13).
							nil -> (call selector: #'test.foo')
						}
						primitiveName: #write
				})
		}
		primitives: primitives.
	expected := program 
		imports: { import alias: #test path: 'test.uzi' isResolved: true }
		globals: { varDec name: #'test.v' value: (number value: 42) }
		scripts: {
			func name: #'test.foo'
				arguments: #()
				body: (block statements: {
					return value: (number value: 42)
				}).
			task name: #main
				arguments: #()
				body: (block statements: {
					call selector: #write
						arguments: {
							nil -> (number value: 13).
							nil -> (call selector: #'test.foo')
						}
						primitiveName: #write
				})
		}
		primitives: primitives.
	actual := ir applyOn: original.
	self assert: expected equals: actual