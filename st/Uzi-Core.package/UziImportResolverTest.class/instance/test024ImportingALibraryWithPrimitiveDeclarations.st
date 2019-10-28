tests
test024ImportingALibraryWithPrimitiveDeclarations
	"
	import a = 'test14.uzi';
	task main() { write(a.add(3, 4), 3 a.~= 4); }
	"
	| original expected actual |
	original := program 
		imports: {
			import alias: #a path: 'test14.uzi' 
		}
		globals: #()
		scripts: {
			task name: #main
				arguments: #()
				body: (block statements: {
					call selector: #write
						arguments: {
							nil -> (call selector: #'a.add' 
										arguments: {
											nil -> (number value: 3).
											nil -> (number value: 4)
										}).
							nil -> (call selector: #'a.~='
										arguments: {
											nil -> (number value: 3).
											nil -> (number value: 4)
										})
						}
				})
		}
		primitives: primitives.
	expected := program 
		imports: { import alias: #a path: 'test14.uzi' isResolved: true }
		globals: #()
		scripts: {
			proc name: #'a.test'
				arguments: #()
				body: (block statements: {
					varDec 
						name: #a
						value: (call 
							selector: #'a.add' 
							arguments: {
								nil -> (number value: 3).
								nil -> (number value: 4)
							}
							primitiveName: #add).
					varDec 
						name: #b
						value: (call 
							selector: #'a.~=' 
							arguments: {
								nil -> (number value: 3).
								nil -> (number value: 4)
							}
							primitiveName: #notEquals).
				}).
			task name: #main
				arguments: #()
				body: (block statements: {
					call selector: #write
						arguments: {
							nil -> (call selector: #'a.add' 
										arguments: {
											nil -> (number value: 3).
											nil -> (number value: 4)
										}
										primitiveName: #add).
							nil -> (call selector: #'a.~='
										arguments: {
											nil -> (number value: 3).
											nil -> (number value: 4)
										}
										primitiveName: #notEquals)
						}
						primitiveName: #write
				})
		}
		primitives: primitives, {
			prim alias: #'a.add' name: #add.
			prim alias: #'a.~=' name: #notEquals
		}.
	actual := ir applyOn: original.
	self assert: expected equals: actual