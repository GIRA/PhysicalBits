operators-mapping
trim: aParser
	"Answer a new parser that consumes and ignores aParser repeatedly before and after the receiving parser."
	
	^ PPTrimmingParser on: self trimmer: aParser