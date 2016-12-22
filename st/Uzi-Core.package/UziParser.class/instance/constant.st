grammar-literals
constant
	^ ($D asParser / $A asParser), integer
		map: [:pinType :pinNumber || offset |
			self flag: #Richo. "This assumes the UNO layout"
			offset := pinType = $A ifTrue: [14] ifFalse: [0].
			UziNumberNode value: pinNumber + offset]