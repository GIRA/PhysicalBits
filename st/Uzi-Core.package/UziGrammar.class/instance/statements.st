grammar
statements
	^ 	(expression wrapped , 
			((separator , 
				statements ==> [ :nodes | nodes first , nodes last ])
					/ optionalSeparator)
						==> [ :nodes | (Array with: nodes first) , (nodes last)])
							/	optionalSeparator