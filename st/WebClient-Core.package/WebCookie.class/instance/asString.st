converting
asString
	"Create the string representation for this cookie"

	^String streamContents:[:s| self printOn: s].