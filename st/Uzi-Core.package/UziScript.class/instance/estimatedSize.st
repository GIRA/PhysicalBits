accessing
estimatedSize
	^ 16 + 
	(self node isTask ifTrue: [20] ifFalse: [0]) +
	(self locals size * 4) +
	(self instructions collect: #estimatedSize) sum
	