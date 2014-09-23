grammar
statements
	^ 	(expression wrapped , 
			(($. asParser uziToken plus , 
				statements ==> [ :nodes | nodes first , nodes last ])
					/ $. asParser uziToken star)
						==> [ :nodes | (Array with: nodes first) , (nodes last)])
							/	($. asParser uziToken star)