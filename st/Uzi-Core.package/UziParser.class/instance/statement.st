grammar-statements
statement
	^ ws, (expression , ws , $; asParser) token , ws
	map: [:ign1 :token :ign2 | 
		"The actual node has ben created but I will update its token so that it
		includes the semicolon"
		token parsedValue first token: token]