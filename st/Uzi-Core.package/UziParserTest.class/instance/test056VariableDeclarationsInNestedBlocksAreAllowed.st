tests
test056VariableDeclarationsInNestedBlocksAreAllowed
	| program script |
	program := parser parse: '
		proc foo() {
			if 1 {
				var a = 1000;
			} else {
				var b = a;
				repeat 10 {
					var c = b * 2;
					b = b + 1;
				}
			}
			while a { var d = c * b; } 
			do { var e = 0; } while d;
			until e { var f; }
			do { var g = f + 1 * b; } until f;
			forever { 
				var h = 5;
			}
		}'.
	self deny: program isPetitFailure.
	script := program scriptNamed: 'foo'.
	self assert: script isProcedure.
	self assert: #(a b c d e f g h) equals: (script locals collect: [:each | each name])