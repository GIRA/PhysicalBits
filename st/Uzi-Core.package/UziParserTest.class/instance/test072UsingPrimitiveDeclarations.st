tests
test072UsingPrimitiveDeclarations
	| ast stmts first second |
	ast := parser parse: '
		var a;
		var b;
		
		prim add;
		prim ~= : notEquals;

		task test() {
			a = add(3, 4);
			b = 3 ~= 4;
		}'.
	self assert: 2 equals: ast primitives size.
	self assert: #add equals: ast primitives first alias.
	self assert: #add equals: ast primitives first name.
	self assert: #~= equals: ast primitives second alias.
	self assert: #notEquals equals: ast primitives second name.
	stmts := ast scripts first body statements.
	first := stmts first right.
	self assert: #add equals: first primitiveName.
	second := stmts second right.
	self assert: #notEquals equals: second primitiveName