decoding
multipartBoundary
	"Answer a string to be used as multpart boundary in posts"

	^'----squeak-', Date today,'-', Time millisecondClockValue, '-webclient-----'.