misc
mcVersion
	"Answers the Monticello version of WebServer's MC package"

	| version |
	Smalltalk at: #MCWorkingCopy ifPresent:[:mc|
		mc managersForClass: self do:[:wc|
			wc ancestors ifNotEmpty:[:ancestry| version := ancestry first name]]].
	^version ifNil:[''].