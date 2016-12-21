fields
postFields
	"Answers the multipart fields of a post request"

	^(self isPostRequest and: [
		self contentType beginsWith: 'application/x-www-form-urlencoded'])
			ifTrue: [WebUtils decodeUrlEncodedForm: self content multipleValues: true]
			ifFalse: [Dictionary new]