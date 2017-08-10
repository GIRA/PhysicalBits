grammar-statements
stopTask
	^ ws , ('stop' asParser , ws , identifier , $; asParser) token , ws		
		map: [:ign1 :token :ign2 |
			(UziScriptStopNode
				script: token parsedValue third)
				token: token]