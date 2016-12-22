grammar-binary
binaryMessage
	^ ws , binarySelector , ws , (unarySend / operand)
		map: [:ign1 :sel :ign2 :arg | self message: sel args: { arg }]