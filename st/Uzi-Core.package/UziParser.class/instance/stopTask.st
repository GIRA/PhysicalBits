grammar-statements
stopTask
	^ ws , ('stop' asParser , ws , scriptList , ws , $; asParser) token , ws		
		map: [:ign1 :token :ign2 |
			(UziScriptStopNode
				scripts: token parsedValue third)
				token: token]