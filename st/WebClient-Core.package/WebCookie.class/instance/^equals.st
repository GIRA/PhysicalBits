comparing
= aCookie
	"Compare the receiver with aCookie"

	^aCookie class == self class 
		and:[aCookie name = self name
		and:[aCookie domain = self domain
		and:[aCookie path = self path]]].