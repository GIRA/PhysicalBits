tests
test037VariablesDeclaredInsideABlockShouldNotCollideWithVariablesDeclaredOutsideAfterTheBlock
	self assertValid: 'task foo() running {
		if 1 { var a = 1; }	
		var a = 2;
	}'.