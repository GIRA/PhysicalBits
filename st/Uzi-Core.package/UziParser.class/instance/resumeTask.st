grammar-statements
resumeTask
	^ ws , ('resume' asParser , ws , scriptList , $; asParser) token , ws
		map: [:ign1 :token :ign2 |
			(UziScriptResumeNode
				scripts: token parsedValue third)
				token: token]