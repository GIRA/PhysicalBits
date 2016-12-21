operators
not
	"Answer a new parser (logical not-predicate) that succeeds whenever the receiver fails, but never consumes input."

	^ PPNotParser on: self