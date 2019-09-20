tests
test003ImportingNestedLibraries
	"
	import t2 from 'test2.uzi';
	task main() { write(D13, t2.bar(1)); }
	"
	| original expected actual |
	original := program 
		imports: {
			import alias: #t2 path: 'test2.uzi' 
		}
		globals: #()
		scripts: {
			task name: #main
				arguments: #()
				body: (block statements: {
					call selector: #write
						arguments: {
							nil -> (number value: 13).
							nil -> (call selector: #'t2.bar'
								arguments: { nil -> (number value: 1) })
						}
						primitiveName: #write
				})
		}.
	expected := program 
		imports: {
			import alias: #t2 path: 'test2.uzi' isResolved: true.
		}
		globals: { 
			varDec name: #'t2.t1.v' value: (number value: 42) 
		}
		scripts: {
			func name: #'t2.t1.foo'
				arguments: #()
				body: (block statements: {
					return value: (number value: 42)
				}).
			func name: #'t2.bar'
				arguments: {
					varDec name: #a
				}
				body: (block statements: {
					return value: (call
						selector: #+
						arguments: {
							nil -> (var named: #'t2.t1.v').
							nil -> (var named: #a)
						}
						primitiveName: #add)
				}).
			task name: #main
				arguments: #()
				body: (block statements: {
					call selector: #write
						arguments: {
							nil -> (number value: 13).
							nil -> (call selector: #'t2.bar'
								arguments: { nil -> (number value: 1) })
						}
						primitiveName: #write
				})
		}.
	actual := ir applyOn: original.
	self assert: expected equals: actual