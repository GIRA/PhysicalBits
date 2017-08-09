grammar-statements
return
	^ ws, ('return' asParser , ws , expression optional, ws , $; asParser) token, ws
		map: [:ign1 :token :ign2 |
			(UziReturnNode 
				value: token parsedValue third)
				token: token]