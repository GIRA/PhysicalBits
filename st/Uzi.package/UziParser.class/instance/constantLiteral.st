literals
constantLiteral
	^ super constantLiteral 
		map: [:pinType :pinNumber || offset pin |
			self flag: #Richo. "This assumes the UNO layout"
			offset := pinType = $A ifTrue: [14] ifFalse: [0].
			pin := pinNumber inputValue asNumber.
			UziNumberNode value: pin + offset]