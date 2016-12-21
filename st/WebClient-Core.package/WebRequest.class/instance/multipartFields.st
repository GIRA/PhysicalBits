fields
multipartFields
	"Answers the multipart fields of a post request"

	| fields |
	fields := Dictionary new.
	self multipartFieldsDo:[:hdrs :params :cntnt|
		params at: 'name' ifPresent:[:fieldName| fields at: fieldName put: cntnt].
	].
	^fields