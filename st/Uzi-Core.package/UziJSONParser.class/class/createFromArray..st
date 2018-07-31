as yet unclassified
createFromArray: anArray
	^ anArray collect: [:each | self createObject: each]