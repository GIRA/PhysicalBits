grammar-literals
constant
	^ ws, (($D asParser / $A asParser), integer) token, ws
		map: [:ign1 :token :ign2 || pinType pinNumber  offset |
			pinType := token parsedValue first.
			pinNumber := token parsedValue second.
			self flag: #Richo. "This assumes the UNO layout"
			offset := pinType = $A ifTrue: [14] ifFalse: [0].
			(UziNumberNode value: pinNumber + offset) token: token]