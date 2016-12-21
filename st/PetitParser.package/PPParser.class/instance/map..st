operators-mapping
map: aBlock
	"Answer a new parser that works on the receiving sequence an passes in each element as a block argument."
	
	^ aBlock numArgs = 1
		ifTrue: [ self ==> aBlock ]
		ifFalse: [ self error: aBlock numArgs asString , ' arguments expected.' ]
