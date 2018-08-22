grammar-literals
constant
	^ ws, (($D asParser / $A asParser), integer) token, ws
		map: [:ign1 :token :ign2 || pinType pinNumber  offset |
			pinType := token parsedValue first asSymbol.
			pinNumber := token parsedValue second.
			(UziPinLiteralNode type: pinType number: pinNumber)
				token: token]