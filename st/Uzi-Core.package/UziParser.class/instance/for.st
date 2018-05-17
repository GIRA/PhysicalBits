grammar-statements
for
	^ ws , 
	('for' asParser , ws , 
	variable , ws , $= asParser, ws , expression , ws , 
	'to' asParser , ws , expression , ws ,
	('by' asParser, ws, expression) optional, ws ,
	block) token ,
	ws
	map: [:ign :token : ign2 || counter start stop step body |
		counter := token parsedValue at: 3.
		start := token parsedValue at: 7.
		stop := token parsedValue at: 11.
		step := (token parsedValue at: 13) ifNotNil: [:a | a last].
		body := token parsedValue at: 15.
		(UziForNode 
			counter: ((UziVariableDeclarationNode 
				name: counter name) 
				token: counter token)
			start: start
			stop: stop
			step: step
			body: body) token: token]