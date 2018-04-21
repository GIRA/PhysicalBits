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
					prim selector: #write
						arguments: {
							number value: 13.
							call script: (ref scriptName: #'t2.bar')
								arguments: { nil -> (number value: 1) }
						}
				})
		}.
	expected := program 
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
					var named: #a
				}
				body: (block statements: {
					return value: (prim
						selector: #+
						arguments: {
							var named: #'t2.t1.v'.
							var named: #a
						}
						primitive: (UziPrimitive named: #add))
				}).
			task name: #main
				arguments: #()
				body: (block statements: {
					prim selector: #write
						arguments: {
							number value: 13.
							call script: (ref scriptName: #'t2.bar') 
								arguments: { nil -> (number value: 1) }
						}
				})
		}.
	actual := ir applyOn: original.
	self assert: expected equals: actual