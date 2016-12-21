private
handle: selector arguments: argList ifAbsent: aBlock

	^(handlers at: selector ifAbsent:[^aBlock value])
		valueWithPossibleArgs: argList.