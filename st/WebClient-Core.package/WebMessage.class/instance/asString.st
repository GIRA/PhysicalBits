converting
asString
	"Prints the receiver"

	^String streamContents:[:s| self writeOn: s].