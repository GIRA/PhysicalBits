grammar-statements
startTask
	^ ws , ('start' asParser , ws , identifier , $; asParser) token , ws		
		map: [:ign1 :token :ign2 |
			(UziScriptStartNode
				script: token parsedValue third)
				token: token]