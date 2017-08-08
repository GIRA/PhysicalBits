grammar-statements
while
	^ ws , 
	('while' asParser , ws , expression , ws , block) token ,
	ws
	map: [:ign :token : ign2 || condition body |
		condition := token parsedValue at: 3.
		body := token parsedValue at: 5.
		(UziWhileNode 
			condition: condition
			body: body) 
			token: token]