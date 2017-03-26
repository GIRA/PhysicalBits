grammar-unary
unaryMessage
	^ ws , (unarySelector , $: asParser not) token
		map: [:ign1 :token || sel |
			sel := token parsedValue first.
			self message: sel args: #() token: token]