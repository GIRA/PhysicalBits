grammar-binary
binaryMessage
	^ ws , (binarySelector , ws , (unarySend / operand)) token
		map: [:ign :token || sel arg |
			sel := token parsedValue first.
			arg := token parsedValue third.
			self message: sel args: { arg } token: token]