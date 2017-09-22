grammar-statements
pauseTask
	^ ws , ('pause' asParser , ws , scriptList , $; asParser) token , ws
		map: [:ign1 :token :ign2 |
			(UziScriptPauseNode
				scripts: token parsedValue third)
				token: token]