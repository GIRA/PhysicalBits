A PPExpressionParser is a parser to conveniently define an expression grammar with prefix, postfix, and left- and right-associative infix operators.

The following code initializes a parser for arithmetic expressions. First we instantiate an expression parser, a simple parser for expressions in parenthesis and a simple parser for integer numbers.

	expression := PPExpressionParser new.
	parens := $( asParser token trim , expression , $) asParser token trim 
		==> [ :nodes | nodes second ].
	integer := #digit asParser plus token trim
		==> [ :token | token value asInteger ].
	
Then we define on what term the expression grammar is built on:

	expression term: parens / integer.
	
Finally we define the operator-groups in descending precedence. Note, that the action blocks receive both, the terms and the parsed operator in the order they appear in the parsed input. 
	
	expression
		group: [ :g |
			g prefix: $- asParser token trim do: [ :op :a | a negated ] ];
		group: [ :g |
			g postfix: '++' asParser token trim do: [ :a :op | a + 1 ].
			g postfix: '--' asParser token trim do: [ :a :op | a - 1 ] ];
		group: [ :g |
			g right: $^ asParser token trim do: [ :a :op :b | a raisedTo: b ] ];
		group: [ :g |
			g left: $* asParser token trim do: [ :a :op :b | a * b ].
			g left: $/ asParser token trim do: [ :a :op :b | a / b ] ];
		group: [ :g |
			g left: $+ asParser token trim do: [ :a :op :b | a + b ].
			g left: $- asParser token trim do: [ :a :op :b | a - b ] ].
		
After evaluating the above code the 'expression' is an efficient parser that evaluates examples like:

	expression parse: '-8++'.
	expression parse: '1+2*3'.
	expression parse: '1*2+3'.
	expression parse: '(1+2)*3'.
	expression parse: '8/4/2'.
	expression parse: '8/(4/2)'.
	expression parse: '2^2^3'.
	expression parse: '(2^2)^3'.
	
Instance Variables:
	operators	<Dictionary>	The operators defined in the current group.