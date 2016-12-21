operators-mapping
==> aBlock
	"Answer a new parser that performs aBlock as action handler on success."

	^ PPActionParser on: self block: aBlock