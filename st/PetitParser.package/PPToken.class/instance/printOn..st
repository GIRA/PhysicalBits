printing
printOn: aStream
	super printOn: aStream.
	aStream nextPut: $[; print: self start; nextPut: $,; print: self stop; nextPut: $].
	aStream nextPut: $(; print: self parsedValue; nextPut: $)