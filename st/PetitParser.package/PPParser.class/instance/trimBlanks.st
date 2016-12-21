operators-mapping
trimBlanks
	"Answer a new parser that consumes blanks before and after the receiving parser."
	
	^ self trim: #blank asParser