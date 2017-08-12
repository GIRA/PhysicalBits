tests
test001
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
					prim selector: #write
						arguments: {
							number value: 13.
							call script: #'test.foo' arguments: #()
						}
				})
		}.
	expected := program 
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
					prim selector: #write
						arguments: {
							number value: 13.
							call script: #'test.foo' arguments: #()
						}
				})
		}.
	actual := ir applyOn: original.
	self assert: expected equals: actual