enumerating
allNodes
	^ Array streamContents: [:stream |
		self do: [:each | stream nextPut: each]]