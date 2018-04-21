grammar-statements
forever
	^ ws , 
	('forever' asParser , ws , block) token ,
	ws
	map: [:ign :token : ign2 || times body |
		body := token parsedValue at: 3.
		(UziForeverNode body: body) token: token]