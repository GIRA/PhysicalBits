grammar-script references
scriptList
	^ (scriptReference separatedBy: (ws , $, asParser , ws))
	==> [:nodes | Array streamContents: [:stream |
			nodes withIndexDo: [:each :index |
				index odd ifTrue: [stream nextPut: each]]]]
		