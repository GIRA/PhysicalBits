grammar-statements
scriptList
	^ ((ws , identifier , ws) separatedBy: (ws , $, asParser , ws))
	==> [:nodes | Array streamContents: [:stream |
			nodes withIndexDo: [:each :index |
				index odd ifTrue: [stream nextPut: each second]]]]
		