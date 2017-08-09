grammar-statements
expressionStatement
	^ (expression , ws , $; asParser) token ==> [:token |
		"The actual node has ben created but I will update its token so that it
		includes the semicolon"
		token parsedValue first token: token]