accessing
nextStatements
	^ Array streamContents: [:stream | self scripts 
		do: [:each | stream nextPutAll: each nextStatements]]