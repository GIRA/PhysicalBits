grammar-statements
startTask
	^ ws , ('start' asParser , ws , scriptList , $; asParser) token , ws		
		map: [:ign1 :token :ign2 |
			(UziScriptStartNode
				scripts: token parsedValue third)
				token: token]